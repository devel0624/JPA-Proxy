package com.nhnacademy.proxyexample.repo;

import com.nhnacademy.proxyexample.entity.DataDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataDetailRepository extends JpaRepository<DataDetail, Long> {
}
