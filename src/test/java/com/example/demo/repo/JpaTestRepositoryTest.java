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

//TODO 주석과 테스트 코드를 확인하여, Proxy 객체에 대해 알아보기
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
     * findById 메서드는 Optional 혹은 orElseThrow 를 사용하여 값을 반환받거나 예외를 발생시킬 수 있다.
     * 즉, 메서드를 호출하는 즉시 데이터베이스를 조회하여 Entity 를 반환한다.
     * 콘솔을 확인하여 메서드 호출 시점에 데이터베이스를 조회하는 SQL 문이 출력되는지 확인.
     * </pre>
     */
    @Test
    @DisplayName("Find By Id 메서드 조회")
    void test2() {

        System.out.println("\n************************** findById 메서드 호출 시작 **************************");
        Optional<Data> existedData = dataRepository.findById(1L);
        Optional<Data> notExistedData = dataRepository.findById(100L);
        System.out.println("************************** findById 메서드 호출 끝 **************************\n");

        assertThat(existedData.isPresent(), is(true));
        assertThat(notExistedData.isPresent(), is(false));
    }

    /**
     * <pre>
     * getReferenceById 메서드는 바로 객체를 반환받을 수 있다.
     * 하지만 findById 와는 다르게 메서드 호출 시점에서 데이터베이스 조회를 하지 않는다. 대신 @ID 값만을 가진 Proxy 객체를 생성하여 반환한다.
     * 따라서 데이터베이스에 존재하지 않는 데이터의 @ID 값이어도 Entity 객체를 생성할 수 있기 때문에, NullPointerException 이 발생하지 않는 것을 확인할 수 있다.
     * 콘솔을 확인하여 메서드 호출 시점에 데이터베이스를 조회하는 SQL 문이 출력되는지 확인.
     * </pre>
     */
    @Test
    @DisplayName("Get Reference By Id 메서드 조회")
    void test3() {

        System.out.println("\n************************** getReferenceById 메서드 호출 시작 **************************");
        Data existedData = dataRepository.getReferenceById(1L);
        Data notExistedData = dataRepository.getReferenceById(100L);
        System.out.println("************************** getReferenceById 메서드 호출 끝 **************************\n");

        assertThat(existedData, notNullValue());
        assertThat(notExistedData, notNullValue());

    }

    /**
     * <pre>
     * Proxy 객체는 @ID 필드를 제외한 필드에 접근하는 시점에 데이터베이스를 조회한다.
     * getId() 와 getName() 메서드를 실행할 때, 로그에 SQL 이 출력되는지 확인
     * </pre>
     */
    @Test
    @DisplayName("Get Reference By Id 메서드로 반환된 객체에 대해, 존재하는 데이터")
    void test4() {
        Data proxy = dataRepository.getReferenceById(1L);

        assertThat(proxy, notNullValue());

        try {
            System.out.println("\n************************** existedData.getId() 메서드 호출 시작 **************************");
            log.info("Existed Data ID : {}", proxy.getId());
            System.out.println("************************** existedData.getId() 메서드 호출 끝 **************************\n");

            System.out.println("\n************************** existedData.getName() 메서드 호출 시작 **************************");
            log.info("Existed Data Name : {}", proxy.getName());
            System.out.println("************************** existedData.getName() 메서드 호출 끝 **************************\n");

        }catch (EntityNotFoundException e){
            log.error("Error Message : {}", e.getMessage());
        }
    }

    /**
     * <pre>
     * 데이터가 실제로 존재하지 않는다면, @ID 외의 필드에 접근시 EntityNotFoundException 이 발생한다.
     * 아래의 테스트를 실행하면 getId() 메서드는 정상적으로 실행되지만, getName() 메서드에서 예외가 발생하여 호출 시작을 알리는 라인만 콘솔에 출력되는 것을 확인할 수 있다.
     * </pre>
     */
    @Test
    @DisplayName("Get Reference By Id 메서드로 반환된 객체에 대해, 존재하지 않는 데이터")
    void test5() {
        Data notExistedData = dataRepository.getReferenceById(100L);

        assertThat(notExistedData, notNullValue());

        try {
            System.out.println("\n************************** notExistedData.getId() 메서드 호출 시작 **************************");
            log.info("Not Existed Data ID : {}", notExistedData.getId());
            System.out.println("************************** notExistedData.getId() 메서드 호출 끝 **************************\n");

            System.out.println("\n************************** notExistedData.getName() 메서드 호출 시작 **************************");
            log.info("Not Existed Data Name : {}", notExistedData.getName());
            System.out.println("************************** notExistedData.getName() 메서드 호출 끝 **************************\n");

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
     * findById() 는 Optional<T> 을 반환하거나 Optional.orElse 메서드 체이닝을 사용할 수 있다.
     *
     * orElse 사용시 다음과 같이 작성 가능. 자세한 내용은 Optional 클래스 참고
     *
     *         findById().orElseThrow(); => IllegalArgumentException 발생
     *         findById().orElseThrow(() -> new RuntimeException());
     *         findById().orElseThrow(RuntimeException::new);
     *         findById().orElse(${Default Entity});
     *         findById().orElseGet( lambda function );
     *
     *
     * getReferenceById() 는 Entity 로 반환 받을 수 있으나, 실제로는 proxy 객체가 반환된다.  
     *
     * 3.  
     * proxy 객체는 @ID 외의 필드 접근 시점에, 데이터베이스에 조회를 요청한다.
     * 연관관계 맵핑 시, FetchType.LAZY 로 지정한 Entity 도 Proxy 객체이므로 같은 방식으로 동작한다.
     *
     * </pre>
     */
    @Test
    @DisplayName("차이점 정리")
    void test6() {


        System.out.println("\n ************* SQL 발생 여부");
        System.out.println("getReferenceById() 메서드 - start");
        Data reference = dataRepository.getReferenceById(1L);
        System.out.println("getReferenceById() 메서드 - end\n");

        System.out.println("\n ************* findById() 메서드 - start");
        Data entity = dataRepository.findById(2L).orElseThrow();
        System.out.println("findById() 메서드 - end\n");

        System.out.println("\n ************* 생성되는 클래스의 형태, reference = Data$HibernateProxy, entity = Data");
        log.info("reference Class : {}", reference.getClass());
        log.info("entity Class : {}", entity.getClass());

        System.out.println("\n ************* Proxy 객체 필드 접근시 SQL 발생");
        log.info("reference Name : {}", reference.getName());

        System.out.println("\n ************* 연관관계 매핑시 FetchType.EAGER 객체 필드 접근시 SQL 발생 여부");
        log.info("reference Information Created At: {}", reference.getDataInformation().getCreatedAt());

        System.out.println("\n ************* 연관관계 매핑시 FetchType.LAZY 객체 필드 접근시 SQL 발생 여부");
        log.info("reference Detail Value : {}", reference.getDataDetail().getValue());


    }

    /**
     * <pre>
     * `GetReferenceById()` 와 `Fetch 전략이 LAZY 로 지정된 연관관계` 같은 proxy 객체 사용시 주의점
     * proxy 객체는 Entity Manager 가 Transaction 내에서 관리하기 때문에, 해당 Transaction 내에서 데이터를 조회하지 않은 상태로
     * Transaction 이 종료되면, 이후 proxy 객체 필드에 접근을 시도할 때, LazyInitializationException 예외가 발생한다.
     *
     *
     * </pre>
     */

    /**
     * detach 전에 proxy 객체의 필드에 접근하지 않아, 데이터 베이스를 조회하지 않았고
     * 이후 해당 객체의 필드에 접근을 시도하면 예외 발생
     */
    @Test
    @DisplayName("준영속 이후 데이터 조회")
    void test7() {
        Data reference = dataRepository.getReferenceById(1L);

        entityManager.detach(reference);

        assertThrows(LazyInitializationException.class, reference::getName);
    }

    /**
     * proxy 객체가 detach 되기 전에 연관 관계 Entity 필드에 접근하면서 데이터 베이스를 조회하였고,
     * 이후 해당 객체의 필드에 접근을 시도해도 예외가 발생하지 않음
     */
    @Test
    @DisplayName("준영속 이전 데이터 조회 후, 준영속 이후 다시 조회")
    void test8() {
        Data reference = dataRepository.getReferenceById(1L);
        log.info("reference Detail Value : {}", reference.getDataDetail().getValue());

        entityManager.detach(reference);

        assertDoesNotThrow(reference::getName);
    }

    /**
     * proxy 객체가 detach 되기 전에 필드에 접근하였지만 @ID 필드를 조회하였기 때문에
     * 데이터 베이스를 조회하지 않았고, 이후 해당 객체의 필드에 접근하면 예외가 발생.
     */
    @Test
    @DisplayName("준영속 이전 ID 필드 조회 후, 준영속 이후 다시 조회")
    void test9() {
        Data reference = dataRepository.getReferenceById(1L);
        log.info("reference ID : {}", reference.getId());

        entityManager.detach(reference);

        assertThrows(LazyInitializationException.class, reference::getName);
    }
}