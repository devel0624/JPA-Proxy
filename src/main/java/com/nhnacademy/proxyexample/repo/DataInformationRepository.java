package com.nhnacademy.proxyexample.repo;

import com.nhnacademy.proxyexample.entity.DataInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataInformationRepository extends JpaRepository<DataInformation, Long> {
}
