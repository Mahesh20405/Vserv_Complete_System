package com.vserv.features.vehicle.repository;

import com.vserv.entity.Vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
	Optional<Vehicle> findByVehicleIdAndIsDeletedFalse(Integer vehicleId);

	long countByIsDeletedFalseAndIsActiveTrue();

	@Query("SELECT v FROM Vehicle v WHERE v.user.userId = :userId AND v.isDeleted = false ORDER BY v.vehicleId DESC")
	List<Vehicle> findByUserIdAndIsDeletedFalse(@Param("userId") Integer userId);

	List<Vehicle> findByIsDeletedFalse();

	long countByIsDeletedFalse();

	boolean existsByRegistrationNumber(String reg);

	boolean existsByRegistrationNumberAndVehicleIdNot(String reg, Integer id);

	@Query("SELECT v FROM Vehicle v WHERE v.isDeleted=false AND (LOWER(v.brand) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(v.model) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(v.registrationNumber) LIKE LOWER(CONCAT('%',:q,'%')))")
	List<Vehicle> search(@Param("q") String q);
}
