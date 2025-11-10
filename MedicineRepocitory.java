// src/main/java/com/app/repository/MedicineRepository.java

package com.app.repository;

import com.app.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Custom Query Method: Spring Data JPA automatically implements this
    List<Medicine> findByStockQuantityLessThan(int minQuantity);

    // Custom Query to find expired medicines
    // @Query("SELECT m FROM Medicine m WHERE m.expiryDate < CURRENT_DATE")
    // List<Medicine> findExpired();
}
