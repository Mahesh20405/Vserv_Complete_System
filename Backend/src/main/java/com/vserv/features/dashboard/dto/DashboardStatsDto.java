package com.vserv.features.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

	// KPIs
	private long totalBookings;
	private long pendingBookings;
	private long confirmedBookings;
	private long completedBookings;
	private long cancelledBookings;
	private long totalUsers;
	private long totalVehicles;
	private long pendingInvoices;
	private long inProgressServices;
	private BigDecimal totalRevenue;

	// Charts - monthly booking counts (last 6 months)
	private List<Map<String, Object>> bookingActivityByMonth;
	// Monthly paid revenue (last 6 months)
	private List<Map<String, Object>> revenueByMonth;
	// Status breakdown
	private Map<String, Long> bookingStatusBreakdown;
	// Service type mix
	private Map<String, Long> serviceMix;
	// Recent bookings (last 10)
	private List<Map<String, Object>> recentBookings;
	// Active advisors (top 5)
	private List<Map<String, Object>> activeAdvisors;

	public long getTotalBookings() {
		return totalBookings;
	}

	public void setTotalBookings(long v) {
		this.totalBookings = v;
	}

	public long getPendingBookings() {
		return pendingBookings;
	}

	public void setPendingBookings(long v) {
		this.pendingBookings = v;
	}

	public long getConfirmedBookings() {
		return confirmedBookings;
	}

	public void setConfirmedBookings(long v) {
		this.confirmedBookings = v;
	}

	public long getCompletedBookings() {
		return completedBookings;
	}

	public void setCompletedBookings(long v) {
		this.completedBookings = v;
	}

	public long getCancelledBookings() {
		return cancelledBookings;
	}

	public void setCancelledBookings(long v) {
		this.cancelledBookings = v;
	}

	public long getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(long v) {
		this.totalUsers = v;
	}

	public long getTotalVehicles() {
		return totalVehicles;
	}

	public void setTotalVehicles(long v) {
		this.totalVehicles = v;
	}

	public long getPendingInvoices() {
		return pendingInvoices;
	}

	public void setPendingInvoices(long v) {
		this.pendingInvoices = v;
	}

	public long getInProgressServices() {
		return inProgressServices;
	}

	public void setInProgressServices(long v) {
		this.inProgressServices = v;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(BigDecimal v) {
		this.totalRevenue = v;
	}

	public List<Map<String, Object>> getBookingActivityByMonth() {
		return bookingActivityByMonth;
	}

	public void setBookingActivityByMonth(List<Map<String, Object>> v) {
		this.bookingActivityByMonth = v;
	}

	public Map<String, Long> getBookingStatusBreakdown() {
		return bookingStatusBreakdown;
	}

	public void setBookingStatusBreakdown(Map<String, Long> v) {
		this.bookingStatusBreakdown = v;
	}

	public List<Map<String, Object>> getRevenueByMonth() {
		return revenueByMonth;
	}

	public void setRevenueByMonth(List<Map<String, Object>> v) {
		this.revenueByMonth = v;
	}

	public Map<String, Long> getServiceMix() {
		return serviceMix;
	}

	public void setServiceMix(Map<String, Long> v) {
		this.serviceMix = v;
	}

	public List<Map<String, Object>> getRecentBookings() {
		return recentBookings;
	}

	public void setRecentBookings(List<Map<String, Object>> v) {
		this.recentBookings = v;
	}

	public List<Map<String, Object>> getActiveAdvisors() {
		return activeAdvisors;
	}

	public void setActiveAdvisors(List<Map<String, Object>> v) {
		this.activeAdvisors = v;
	}
}
