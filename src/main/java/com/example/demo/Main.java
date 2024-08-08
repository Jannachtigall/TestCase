package com.example.demo;

import com.example.demo.pojo.Ticket;
import com.example.demo.pojo.TicketsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    static Map<String, Long> minimalDateTimeForCarriers = new HashMap<>();
    public static void main(String[] args) {

        List<Ticket> ticketList;

        ticketList = deserialization("tickets.json");

        ticketList = dataTransformation(ticketList);

        double median = medianFinding(ticketList);

        double average = dataAnalysis(ticketList);

        // Вывод данных
        for (String carrier:
             minimalDateTimeForCarriers.keySet()) {
            System.out.println("Для перевозчика " + carrier
                    + " минимальное время полёта равно "
                    + (minimalDateTimeForCarriers.get(carrier) / 60) + " часов "
                    + (minimalDateTimeForCarriers.get(carrier) % 60) + " минут.");
        }
        System.out.println("Разница между медианой и средней ценой полёта между искомыми городами равна " + Math.abs(median - average));
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

        // Удаляем из списка все билеты, кроме билетов из Владивостока в Тель-Авив
        ticketList = ticketList.stream().filter(x ->
                        x.getOrigin().equals("VVO") && x.getDestination().equals("TLV"))
                .collect(Collectors.toList());

        // Для нахождения медианы сортируем весь список по возрастанию стоимости билета
        ticketList.sort(Comparator.comparingInt(Ticket::getPrice));
        return ticketList;
    }

    public static double medianFinding(List<Ticket> ticketList) {
        double median;

        // Находим медиану - это срединное число в упоряденном по возрастанию ряду,
        // либо среднее от суммы двух срединных чисел в ряду с чётным количеством элементов
        if (ticketList.size() % 2 != 0) {
            median = ticketList.get(ticketList.size() / 2).getPrice();
        } else {
            int right = ticketList.get(ticketList.size() / 2).getPrice();
            int left = ticketList.get(ticketList.size() / 2 - 1).getPrice();
            median = (double) (right + left) / 2;
        }
        return median;
    }

    public static double dataAnalysis(List<Ticket> ticketList) {
        double average = 0;

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
        return average;
    }


}