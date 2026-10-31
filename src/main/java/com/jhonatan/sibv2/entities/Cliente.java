package com.jhonatan.sibv2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class Cliente extends Pessoas {}
