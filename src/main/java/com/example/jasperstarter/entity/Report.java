package com.example.jasperstarter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reports")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String format;
    private String name;
    private String size;
    @Lob
    private byte[] reportData;
}