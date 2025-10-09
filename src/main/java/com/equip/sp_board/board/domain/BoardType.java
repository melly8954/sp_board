package com.equip.sp_board.board.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="board_type_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_type_id")
    private Long boardTypeId;
    private String name;
    private String description;
}
