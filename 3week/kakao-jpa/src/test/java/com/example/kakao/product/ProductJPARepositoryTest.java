package com.example.kakao.product;

import com.example.kakao._core.util.DummyEntity;
import com.example.kakao.product.option.Option;
import com.example.kakao.product.option.OptionJPARepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Import(ObjectMapper.class)
@DataJpaTest
public class ProductJPARepositoryTest extends DummyEntity {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductJPARepository productJPARepository;

    @Autowired
    private OptionJPARepository optionJPARepository;

    @Autowired
    private ObjectMapper om;

    @BeforeEach
    public void setUp(){
        List<Product> productListPS = productJPARepository.saveAll(productDummyList());
        optionJPARepository.saveAll(optionDummyList(productListPS));
        em.clear();
    }

    @DisplayName("전체 상품 불러오기 테스트")
    @Test
    public void product_findAll_test() throws JsonProcessingException {
        // given
        int page = 0;
        int size = 9;

        // when
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> productPG = productJPARepository.findAll(pageRequest);
        String responseBody = om.writeValueAsString(productPG);
        System.out.println("테스트 : "+responseBody);

        // then
        Assertions.assertThat(productPG.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(productPG.getSize()).isEqualTo(9);
        Assertions.assertThat(productPG.getNumber()).isEqualTo(0);
        Assertions.assertThat(productPG.getTotalElements()).isEqualTo(15);
        Assertions.assertThat(productPG.isFirst()).isEqualTo(true);
        Assertions.assertThat(productPG.getContent().get(0).getId()).isEqualTo(1);
        Assertions.assertThat(productPG.getContent().get(0).getProductName()).isEqualTo("기본에 슬라이딩 지퍼백 크리스마스/플라워에디션 에디션 외 주방용품 특가전");
        Assertions.assertThat(productPG.getContent().get(0).getDescription()).isEqualTo("");
        Assertions.assertThat(productPG.getContent().get(0).getImage()).isEqualTo("/images/1.jpg");
        Assertions.assertThat(productPG.getContent().get(0).getPrice()).isEqualTo(1000);
    }

    // ManyToOne 전략을 Eager로 간다면 추천
    @Test
    public void option_findByProductId_eager_test() throws JsonProcessingException {
        // given
        int id = 1;

        // when
        // 충분한 데이터 - product만 0번지에서 빼면  된다
        // 조인은 하지만, fetch를 하지 않아서, product를 한번 더 select 했다.
        List<Option> optionListPS = optionJPARepository.findByProductId(id); // Eager

        System.out.println("json 직렬화 직전========================");
        String responseBody = om.writeValueAsString(optionListPS);
        System.out.println("테스트 : "+responseBody);

        // then
    }

    @Test
    public void option_findByProductId_lazy_error_test() throws JsonProcessingException {
        // given
        int id = 1;

        // when
        // option을 select했는데, product가 lazy여서 없는 상태이다.
        List<Option> optionListPS = optionJPARepository.findByProductId(id); // Lazy

        // product가 없는 상태에서 json 변환을 시도하면 (hibernate는 select를 요청하는데, json mapper는 json 변환을 시도하게 된다)
        // 이때 json 변환을 시도하는 것이 타이밍적으로 더 빠르다 (I/O)가 없기 때문에!!
        // 그래서 hibernateLazyInitializer 오류가 발생한다.
        // 그림 설명 필요
        System.out.println("json 직렬화 직전========================");
        String responseBody = om.writeValueAsString(optionListPS);
        System.out.println("테스트 : "+responseBody);

        // then
    }

    // 추천
    // 조인쿼리 직접 만들어서 사용하기
    @DisplayName("옵션들 가져오기 테스트")
    @Test
    public void option_mFindByProductId_lazy_test() throws JsonProcessingException {
        // given
        int id = 1;

        // when
        List<Option> optionListPS = optionJPARepository.mFindByProductId(id); // Lazy

        System.out.println("json 직렬화 직전========================");
        String responseBody = om.writeValueAsString(optionListPS);
        System.out.println("테스트 : "+responseBody);

        // then
        Assertions.assertThat(optionListPS.get(0).getId()).isEqualTo(1);
        Assertions.assertThat(optionListPS.get(0).getOptionName()).isEqualTo("01. 슬라이딩 지퍼백 크리스마스에디션 4종");
        Assertions.assertThat(optionListPS.get(0).getPrice()).isEqualTo(10000);

        Assertions.assertThat(optionListPS.get(1).getId()).isEqualTo(2);
        Assertions.assertThat(optionListPS.get(1).getOptionName()).isEqualTo("02. 슬라이딩 지퍼백 플라워에디션 5종");
        Assertions.assertThat(optionListPS.get(1).getPrice()).isEqualTo(10900);

        Assertions.assertThat(optionListPS.get(2).getId()).isEqualTo(3);
        Assertions.assertThat(optionListPS.get(2).getOptionName()).isEqualTo("고무장갑 베이지 S(소형) 6팩");
        Assertions.assertThat(optionListPS.get(2).getPrice()).isEqualTo(9900);

        Assertions.assertThat(optionListPS.get(3).getId()).isEqualTo(4);
        Assertions.assertThat(optionListPS.get(3).getOptionName()).isEqualTo("뽑아쓰는 키친타올 130매 12팩");
        Assertions.assertThat(optionListPS.get(3).getPrice()).isEqualTo(16900);

        Assertions.assertThat(optionListPS.get(4).getId()).isEqualTo(5);
        Assertions.assertThat(optionListPS.get(4).getOptionName()).isEqualTo("2겹 식빵수세미 6매");
        Assertions.assertThat(optionListPS.get(4).getPrice()).isEqualTo(8900);
    }


    // 추천
    @Test
    public void product_findById_and_option_findByProductId_lazy_test() throws JsonProcessingException {
        // given
        int id = 1;

        // when
        Product productPS = productJPARepository.findById(id).orElseThrow(
                () -> new RuntimeException("상품을 찾을 수 없습니다")
        );

        // product 상품은 영속화 되어 있어서, 아래에서 조인해서 데이터를 가져오지 않아도 된다.
        List<Option> optionListPS = optionJPARepository.findByProductId(id); // Lazy

        String responseBody1 = om.writeValueAsString(productPS);
        String responseBody2 = om.writeValueAsString(optionListPS);
        System.out.println("테스트 : "+responseBody1);
        System.out.println("테스트 : "+responseBody2);

        // then
    }

}
