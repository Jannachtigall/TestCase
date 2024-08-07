package com.example.demo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    static Map<String, Long> minimalDateTimeForCarriers = new HashMap<>();
    static double median;
    static double average;
    public static void main(String[] args) {

        List<Ticket> ticketList = deserialization("tickets.json");

        ticketList = dataTransformation(ticketList);

        medianAndAverageFinding(ticketList);

        dataAnalysis(ticketList);

        // Вывод данных
        for (String carrier:
             minimalDateTimeForCarriers.keySet()) {
            System.out.println("For carrier " + carrier
                    + " the minimum flight time is "
                    + (minimalDateTimeForCarriers.get(carrier) / 60) + " hours "
                    + (minimalDateTimeForCarriers.get(carrier) % 60) + " minutes.");
        }
        System.out.println("The difference between the average price and the median for a flight is " + Math.abs(median - average));
    }

    // С помощью фреймворка jackson сериализуем данные из файла в массив pojo-объектов
    public static List<Ticket> deserialization(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        File file = new File(filename);
        TicketsResponse ticketsResponse;
        try {
            ticketsResponse = mapper.readValue(file, TicketsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ticketsResponse.getTickets();
    }

    public static List<Ticket> dataTransformation(List<Ticket> ticketList) {

        // Удаляем из списка все билеты, кроме билетом из Владивостока в Тель-Авив
        ticketList = ticketList.stream().filter(x ->
                        x.getOrigin().equals("VVO") && x.getDestination().equals("TLV"))
                .collect(Collectors.toList());

        // Для нахождения медианы сортируем весь список по возрастанию стоимости билета
        ticketList.sort(Comparator.comparingInt(Ticket::getPrice));
        return ticketList;
    }

    public static void medianAndAverageFinding(List<Ticket> ticketList) {

        // Находим медиану - это срединное число в упоряденном по возрастанию ряду,
        // либо среднее от суммы двух срединных чисел в ряду с чётным количеством элементов
        if (ticketList.size() % 2 != 0) {
            median = ticketList.get(ticketList.size() / 2).getPrice();
        } else {
            int right = ticketList.get(ticketList.size() / 2).getPrice();
            int left = ticketList.get(ticketList.size() / 2 - 1).getPrice();
            median = (double) (right + left) / 2;
        }
    }

    public static void dataAnalysis(List<Ticket> ticketList) {
        for (Ticket ticket:
                ticketList) {

            LocalDateTime departureDateTime = LocalDateTime.of(ticket.getDepartureDate(), ticket.getDepartureTime());
            LocalDateTime arrivalDateTime = LocalDateTime.of(ticket.getArrivalDate(), ticket.getArrivalTime());
            Long difference = Duration.between(departureDateTime, arrivalDateTime).toMinutes();
            if (!minimalDateTimeForCarriers.containsKey(ticket.getCarrier())
                    || difference < minimalDateTimeForCarriers.get(ticket.getCarrier())) {
                minimalDateTimeForCarriers.put(ticket.getCarrier(), difference);
            }

            // Суммируем цены
            average += ticket.getPrice();
        }

        // Получаем среднюю цену
        average /= ticketList.size();
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class Ticket {
        private String origin;

        @JsonProperty("origin_name")
        private String originName;

        private String destination;

        @JsonProperty("destination_name")
        private String destinationName;

        @JsonProperty("departure_date")
        @JsonFormat(pattern = "dd.MM.yy")
        private LocalDate departureDate;

        @JsonProperty("departure_time")
        @JsonFormat(pattern = "H:mm")
        private LocalTime departureTime;

        @JsonProperty("arrival_date")
        @JsonFormat(pattern = "dd.MM.yy")
        private LocalDate arrivalDate;

        @JsonProperty("arrival_time")
        @JsonFormat(pattern = "H:mm")
        private LocalTime arrivalTime;

        private String carrier;
        private int stops;
        private int price;

        public Ticket() {}

        @Override
        public String toString() {
            return "Ticket{" +
                    "origin='" + origin + '\'' +
                    ", originName='" + originName + '\'' +
                    ", destination='" + destination + '\'' +
                    ", destinationName='" + destinationName + '\'' +
                    ", departureDate=" + departureDate +
                    ", departureTime=" + departureTime +
                    ", arrivalDate=" + arrivalDate +
                    ", arrivalTime=" + arrivalTime +
                    ", carrier='" + carrier + '\'' +
                    ", stops=" + stops +
                    ", price=" + price +
                    '}';
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public String getOriginName() {
            return originName;
        }

        public void setOriginName(String originName) {
            this.originName = originName;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getDestinationName() {
            return destinationName;
        }

        public void setDestinationName(String destinationName) {
            this.destinationName = destinationName;
        }

        public LocalDate getDepartureDate() {
            return departureDate;
        }

        public void setDepartureDate(LocalDate departureDate) {
            this.departureDate = departureDate;
        }

        public LocalTime getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(LocalTime departureTime) {
            this.departureTime = departureTime;
        }

        public LocalDate getArrivalDate() {
            return arrivalDate;
        }

        public void setArrivalDate(LocalDate arrivalDate) {
            this.arrivalDate = arrivalDate;
        }

        public LocalTime getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(LocalTime arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public String getCarrier() {
            return carrier;
        }

        public void setCarrier(String carrier) {
            this.carrier = carrier;
        }

        public int getStops() {
            return stops;
        }

        public void setStops(int stops) {
            this.stops = stops;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }

    @JsonAutoDetect
    static class TicketsResponse {

        private List<Ticket> tickets;

        public TicketsResponse() {
        }

        public List<Ticket> getTickets() {
            return tickets;
        }

        public void setTickets(List<Ticket> tickets) {
            this.tickets = tickets;
        }
    }

}