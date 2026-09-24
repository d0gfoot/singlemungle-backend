package com.singlemungle.backend.domain.place.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "place_business_hours",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_place_business_hours_place_id_day_of_week",
			columnNames = {"place_id", "day_of_week"}
		)
	}
)
public class PlaceBusinessHour {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "place_id", nullable = false)
	private Place place;

	@Enumerated(EnumType.STRING)
	@Column(name = "day_of_week", nullable = false, length = 20)
	private DayOfWeek dayOfWeek;

	@Column(name = "open_time")
	private LocalTime openTime;

	@Column(name = "close_time")
	private LocalTime closeTime;

	@Column(name = "break_start_time")
	private LocalTime breakStartTime;

	@Column(name = "break_end_time")
	private LocalTime breakEndTime;

	@Column(name = "is_closed", nullable = false)
	private boolean closed = false;

	protected PlaceBusinessHour() {
	}

	public PlaceBusinessHour(
		Place place,
		DayOfWeek dayOfWeek,
		LocalTime openTime,
		LocalTime closeTime,
		LocalTime breakStartTime,
		LocalTime breakEndTime,
		boolean closed
	) {
		this.place = place;
		this.dayOfWeek = dayOfWeek;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.breakStartTime = breakStartTime;
		this.breakEndTime = breakEndTime;
		this.closed = closed;
	}

	public Long getId() {
		return id;
	}

	public Place getPlace() {
		return place;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public LocalTime getOpenTime() {
		return openTime;
	}

	public LocalTime getCloseTime() {
		return closeTime;
	}

	public LocalTime getBreakStartTime() {
		return breakStartTime;
	}

	public LocalTime getBreakEndTime() {
		return breakEndTime;
	}

	public boolean isClosed() {
		return closed;
	}
}
