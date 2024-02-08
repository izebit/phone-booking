package ru.izebit.mobile.booking.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "PHONES")
@Data
public class Phone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;
}
