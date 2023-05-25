package com.example.demo.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "`Data`")
public class Data {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_id")
    private Long id;

    private String name;

//    기본 fetch 전략 EAGER
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_detail_id")
    private DataDetail dataDetail;

    @OneToOne
    @JoinColumn(name = "data_information_id")
    private DataInformation dataInformation;
}
