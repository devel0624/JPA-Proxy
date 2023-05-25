package com.example.demo.repo;

import com.example.demo.config.JpaConfig;
import com.example.demo.entity.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Transactional
@ContextHierarchy(
        @ContextConfiguration(classes = {JpaConfig.class})
)
@ExtendWith(SpringExtension.class)
class JpaTestRepositoryTest {

    @Autowired
    DataRepository dataRepository;

    @Autowired
    EntityManager entityManager;

    /**
     * <pre>
     * Connection 연결 및 init Sql 실행 확인용 메서드.
     * </pre>
     */
    @Test
    @DisplayName("데이터베이스에 있는 값들의 ID 확인")
    void test1(){
        List<Data> list = dataRepository.findAll();

        assertThat(list, not(empty()));

        list.forEach(x -> log.error("Data ID : {}", x.getId()));
    }

    /**
     * <pre>
     * findById 메서드는 Optional 혹은 orElseThrow 를 사용하여 값을 반환받을 수 있다.  
     * 메서드를 호출하는 즉시 데이터베이스에 조회 요청을 한다.  
     * 콘솔을 확인하여 메서드 호출 시점에 데이터베이스에 요청이 발생하고 있는지 확인.
     * </pre>
     */
    @Test
    @DisplayName("Find By Id 메서드 조회")
    void test2() {

        System.out.println("#################################### findById 메서드 호출 시작 ####################################");
        Optional<Data> existedData = dataRepository.findById(1L);
        Optional<Data> notExistedData = dataRepository.findById(100L);
        System.out.println("#################################### findById 메서드 호출 끝 ####################################");

        assertThat(existedData.isPresent(), is(true));
        assertThat(notExistedData.isPresent(), is(false));
    }

    /**
     * <pre>
     * getReferenceById 메서드는 특별한 처리 없이 객체를 생성할 수 있다. 이때 객체는 ID 정보만을 가진 Proxy 객체로 생성된다.  
     * 메서드를 호출하는 시점에는 아이디 값을 가진 Proxy 객체를 생성하고 데이터베이스에 조회 요청을 하지 않는다.  
     * 따라서 실제로는 존재하지 않는 데이터라고 해도 NullPointerException 이 발생하지 않는 것을 확인할 수 있다.  
     * 콘솔을 확인하여 메서드 호출 시점에 데이터베이스에 요청이 발생하고 있는지 확인.
     * </pre>
     */
    @Test
    @DisplayName("Get Reference By Id 메서드 조회")
    void test3() {

        System.out.println("#################################### getReferenceById 메서드 호출 시작 ####################################");
        Data existedData = dataRepository.getReferenceById(1L);
        Data notExistedData = dataRepository.getReferenceById(100L);
        System.out.println("#################################### getReferenceById 메서드 호출 끝 ####################################");

        assertThat(existedData, notNullValue());
        assertThat(notExistedData, notNullValue());

    }

    /**
     * <pre>
     * 프록시 객체는 ID 를 제외한 필드에 접근할 때, 데이터베이스에 조회 요청을 보낸다.
     * </pre>
     */
    @Test
    @DisplayName("Get Reference By Id 메서드로 반환된 객체에 대해, 존재하는 데이터")
    void test4() {
        Data proxy = dataRepository.getReferenceById(1L);

        assertThat(proxy, notNullValue());

        try {
            System.out.println("#################################### existedData.getId() 메서드 호출 시작 ####################################");
            log.info("Existed Data ID : {}", proxy.getId());
            System.out.println("#################################### existedData.getId() 메서드 호출 끝 ####################################");

            System.out.println("#################################### existedData.getName() 메서드 호출 시작 ####################################");
            log.info("Existed Data Name : {}", proxy.getName());
            System.out.println("#################################### existedData.getName() 메서드 호출 끝 ####################################");

        }catch (EntityNotFoundException e){
            log.error("Error Message : {}", e.getMessage());
        }
    }

    /**
     * <pre>
     * 따라서 데이터가 실제로 존재하지 않는다면, ID 외의 필드에 접근시 EntityNotFoundException 이 발생한다.  
     * 아래의 테스트를 실행하면 getName 호출 시작을 알리는 줄만 출력되는 것을 확인할 수 있다.
     * </pre>
     */
    @Test
    @DisplayName("Get Reference By Id 메서드로 반환된 객체에 대해, 존재하지 않는 데이터")
    void test5() {
        Data notExistedData = dataRepository.getReferenceById(100L);

        assertThat(notExistedData, notNullValue());

        try {
            System.out.println("#################################### notExistedData.getId() 메서드 호출 시작 ####################################");
            log.info("Not Existed Data ID : {}", notExistedData.getId());
            System.out.println("#################################### notExistedData.getId() 메서드 호출 끝 ####################################");

            System.out.println("#################################### notExistedData.getName() 메서드 호출 시작 ####################################");
            log.info("Not Existed Data Name : {}", notExistedData.getName());
            System.out.println("#################################### notExistedData.getName() 메서드 호출 끝 ####################################");

        }catch (EntityNotFoundException e){
            log.error("Error Message : {}", e.getMessage());
        }
    }

    /**
     * <pre>
     * 이전 테스트들을 바탕으로 정리  
     *
     * 1.  
     * findById() 는 메서드 실행과 함께 데이터베이스에 조회를 요청한다.
     * getReferenceById() 는 proxy 객체를 반환하기 때문에 조회를 요청하지 않음.
     *
     * 2.
     * findById() 는 Optional<Entity> 을 반환하거나 IllegalArgumentException 이 발생,
     *
     * 예외 발생시 다음과 같이 작성 가능
     *
     *         findById().orElseThrow(); => IllegalArgumentException 발생
     *         findById().orElseThrow(() -> new EntityNotFoundException());
     *         findById().orElseThrow(EntityNotFoundException::new);
     *
     * getReferenceById() 는 Entity 로 반환 받을 수 있으나, 실제로는 proxy 객체가 반환된다.  
     *
     * 3.  
     * proxy 객체는 ID 필드 외의 필드에 접근할 때, 데이터베이스에 조회를 요청한다.
     * 연관관계 매핑시 FetchType.LAZY 로 지정한 Entity 도 Proxy 객체이므로 같은 방식으로 동작한다.
     *
     * </pre>
     */
    @Test
    @DisplayName("차이점 정리")
    void test6() {

        System.out.println("SQL 발생 여부");
        System.out.println("getReferenceById() 메서드 - start");
        Data reference = dataRepository.getReferenceById(1L);
        System.out.println("getReferenceById() 메서드 - end");

        System.out.println("findById() 메서드 - start");
        Data entity = dataRepository.findById(2L).orElseThrow();
        System.out.println("findById() 메서드 - end");

        System.out.println("생성되는 클래스의 형태, reference = Data$HibernateProxy, entity = Data");
        log.info("reference Class : {}", reference.getClass());
        log.info("entity Class : {}", entity.getClass());

        System.out.println("Proxy 객체 필드 접근시 SQL 발생");
        log.info("reference Name : {}", reference.getName());

        System.out.println("연관관계 매핑시 FetchType.EAGER 객체 필드 접근시 SQL 발생 여부");
        log.info("reference Information Created At: {}", reference.getDataInformation().getCreatedAt());

        System.out.println("연관관계 매핑시 FetchType.LAZY 객체 필드 접근시 SQL 발생 여부");
        log.info("reference Detail Value : {}", reference.getDataDetail().getValue());


    }

    /**
     * <pre>
     * GetReferenceById(), FetchType.LAZY 연관관계와 같은 proxy 객체 사용시 주의점
     * proxy 객체는 Entity Manager 가 Transaction 내에서 관리하기 때문에, 해당 Transaction 내에서 1회라도 데이터를 조회하지 않았다면
     * Transaction 이 종료된 이후 객체의 필드에 접근을 시도하면 LazyInitializationException 예외가 발생한다.
     * </pre>
     */

    /**
     * proxy 객체가 detach 되기 전에 필드에 접근하지 않아 콘솔에 SQL 이 출력되지 않았고, 이후 해당 객체의 필드에 접근을 시도하면 예외 발생
     */
    @Test
    void test7() {
        Data reference = dataRepository.getReferenceById(1L);

        entityManager.detach(reference);

        assertThrows(LazyInitializationException.class, reference::getName);
    }

    /**
     * proxy 객체가 detach 되기 전에 연관 관계 Entity 필드에 접근하면서 SQL 이 출력되었고, 이후 해당 객체의 필드에 접근을 시도해도 예외가 발생하지 않음
     */
    @Test
    void test8() {
        Data reference = dataRepository.getReferenceById(1L);
        log.info("reference Detail Value : {}", reference.getDataDetail().getValue());

        entityManager.detach(reference);

        assertDoesNotThrow(reference::getName);
    }

    /**
     * proxy 객체가 detach 되기 전에 필드에 접근하였지만 ID 필드를 조회하였기 때문에
     * SQL 이 출력되지 않았고 이후 해당 객체의 필드에 접근을 시도해도 예외가 발생.
     */
    @Test
    void test9() {
        Data reference = dataRepository.getReferenceById(1L);
        log.info("reference ID : {}", reference.getId());

        entityManager.detach(reference);

        assertThrows(LazyInitializationException.class, reference::getName);
    }
}