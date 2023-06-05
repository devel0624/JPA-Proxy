package com.nhnacademy.proxyexample.entity;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "`Data_Detail`")
public class DataDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_detail_id")
    private Long id;

    private String value;
}
