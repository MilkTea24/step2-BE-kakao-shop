package com.example.kakao.cart;

import com.example.kakao.product.option.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CartJPARepository extends JpaRepository<Cart, Integer> {
    List<Cart> findAllByUserId(int userId);

    @Query("select c from Cart c join fetch c.option o join fetch o.product where c.user.id = :userId order by o.id asc")
    List<Cart> findAllByUserIdJoinOptionJoinProduct(@Param("userId") int userId);

    @Query("select c from Cart c where c.user.id = :userId order by c.option.id asc")
    List<Cart> findByUserIdOrderByOptionIdAsc(int userId);

    @Modifying
    @Query("delete from Cart c where c.user.id = :userId")
    void deleteAllByUserId(int userId);

    @Query("select c from Cart c where c.option.id = :optionId and c.user.id = :userId")
    Optional<Cart> findByOptionIdAndUserId(@Param("optionId") int optionId, @Param("userId") int userId);
}