package com.nhnacademy.proxyexample.entity;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "`Data_Information`")
public class DataInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_information_id")
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
