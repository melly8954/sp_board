package com.equip.sp_board.board.controller;

import com.equip.sp_board.common.controller.ResponseController;
import com.equip.sp_board.common.dto.ResponseDto;
import com.equip.sp_board.common.trace.RequestTraceIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class BoardController implements ResponseController {
    
}
