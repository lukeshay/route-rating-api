package com.routerating.api.ratings.route;

import com.routerating.api.common.routes.RouteRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRatingRepository extends JpaRepository<RouteRating, String> {

	List<RouteRating> findAllByRouteId(String routeId);

	Page<RouteRating> findAllByRouteId(Pageable pageable, String routeId);
}
