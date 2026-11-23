package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.responses.BackupResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BackupService {
    private static final DateTimeFormatter FORMATO_ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final JdbcTemplate jdbcTemplate;

    public BackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BackupResponse criarBackup() {
        String diretorio = consultarPastaPadrao();
        LocalDateTime agora = LocalDateTime.now();
        String arquivo = "sib_backup_" + agora.format(FORMATO_ARQUIVO) + ".bak";

        try {
            jdbcTemplate.execute("BACKUP DATABASE brecho"
                    + " TO DISK = N'" + arquivo + "' WITH COPY_ONLY, CHECKSUM, INIT");
        } catch (DataAccessException erro) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "O SQL Server não conseguiu gerar o backup. Verifique a permissão BACKUP DATABASE e a permissão de gravação da conta do serviço na pasta de backup.",
                    erro);
        }

        String caminho = diretorio == null || diretorio.isBlank()
                ? "Pasta padrão de backup da instância SQL Server"
                : diretorio + (diretorio.endsWith("\\") || diretorio.endsWith("/") ? "" : "\\") + arquivo;
        return new BackupResponse(arquivo, caminho,
                agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public void restaurarBackup(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um arquivo de backup.");
        }
        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase().endsWith(".bak")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um arquivo com extensão .bak.");
        }

        String diretorio = consultarPastaPadrao();
        if (diretorio == null || diretorio.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível localizar a pasta padrão de backup do SQL Server.");
        }

        Path pastaBackup = Path.of(diretorio);
        if (!Files.isDirectory(pastaBackup)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A API precisa acessar a pasta padrão de backup do SQL Server para importar o arquivo.");
        }

        Path arquivoTemporario = null;
        try {
            arquivoTemporario = Files.createTempFile(pastaBackup, "sib_import_", ".bak");
            try (var conteudo = arquivo.getInputStream()) {
                Files.copy(conteudo, arquivoTemporario, StandardCopyOption.REPLACE_EXISTING);
            }
            restaurarBanco(arquivoTemporario);
        } catch (IOException erro) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível copiar o backup para a pasta do SQL Server.", erro);
        } catch (DataAccessException erro) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "O SQL Server não conseguiu restaurar o arquivo. Confira as permissões e se o backup está íntegro.", erro);
        } finally {
            if (arquivoTemporario != null) {
                try {
                    Files.deleteIfExists(arquivoTemporario);
                } catch (IOException ignored) {

                }
            }
        }
    }

    private void restaurarBanco(Path arquivo) {
        String caminhoSql = arquivo.toString().replace("'", "''");
        String origemSql = "N'" + caminhoSql + "'";
        jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) conexao -> {
            try {
                conexao.setCatalog("master");
                try (Statement comando = conexao.createStatement()) {
                    validarArquivoBackup(comando, origemSql);
                    comando.execute("RESTORE VERIFYONLY FROM DISK = " + origemSql + " WITH CHECKSUM");
                    comando.execute("ALTER DATABASE brecho SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
                    try {
                        comando.execute("RESTORE DATABASE brecho FROM DISK = " + origemSql
                                + " WITH REPLACE, CHECKSUM");
                    } finally {
                        comando.execute("ALTER DATABASE brecho SET MULTI_USER");
                    }
                }
            } finally {
                // A conexão pertence ao pool da aplicação; devolva-a ao catálogo usado pelo sistema.
                conexao.setCatalog("brecho");
            }
            return null;
        });
    }

    private void validarArquivoBackup(Statement comando, String origemSql) throws SQLException {
        try (ResultSet cabecalho = comando.executeQuery("RESTORE HEADERONLY FROM DISK = " + origemSql)) {
            if (!cabecalho.next()
                    || !"brecho".equalsIgnoreCase(cabecalho.getString("DatabaseName"))
                    || cabecalho.getInt("BackupType") != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O arquivo precisa conter um backup completo do banco brecho.");
            }
        }
    }

    private String consultarPastaPadrao() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT CAST(SERVERPROPERTY('InstanceDefaultBackupPath') AS NVARCHAR(4000))",
                    String.class);
        } catch (DataAccessException ignored) {
            return null;
        }
    }
}
