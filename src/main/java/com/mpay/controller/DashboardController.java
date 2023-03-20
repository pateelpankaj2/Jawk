package com.mpay.controller;

import com.mpay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/dashboard")
public class DashboardController {

	@Autowired
	private OrderService orderService;

	@GetMapping
	private ResponseEntity<?> getOrderStatsByTypeAndId(@RequestParam(name = "type") String type,
			@RequestParam(name = "id", required = false) Long id) {
		Map<String, Object> orderStats = orderService.getOrderStats(type, id);
		return new ResponseEntity<>(orderStats, HttpStatus.OK);
	}

	@GetMapping("/recent-order")
	private ResponseEntity<?> getRecentOrder(@RequestParam(name = "type") String type,
			@RequestParam(name = "id", required = false) Long id) {
		List<Object> recentOrder = orderService.getRecentOrder(type, id);
		return new ResponseEntity<>(recentOrder, HttpStatus.OK);
	}

	@GetMapping("/order-analytics")
	private ResponseEntity<?> getOrderAnalytics(@RequestParam(name = "type") String type,
			@RequestParam(name = "id") Long id, @RequestParam("timeframe") String timeframe) {
		Map<String, Object> orderAnalytics = orderService.getOrderAnalytics(type, id, timeframe);
		return new ResponseEntity<>(orderAnalytics, HttpStatus.OK);
	}
}
