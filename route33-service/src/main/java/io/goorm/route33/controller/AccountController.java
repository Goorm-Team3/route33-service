package io.goorm.route33.controller;

import io.goorm.route33.auth.Auth;
import io.goorm.route33.model.dto.AccountInfoResponseDto;
import io.goorm.route33.model.dto.AccountRequestDto;
import io.goorm.route33.model.dto.CustomResponseDto;
import io.goorm.route33.service.AccountService;
import io.goorm.route33.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 계좌 관련 컨트롤러
 */
@Slf4j
@RequestMapping("/account")
@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final TokenService tokenService;

    /**
     * 계좌 정보 조회를 요청한다.
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<?> accountInfo(@RequestHeader("Authorization") String token) {
        long userId = tokenService.extractUserId(token);
        AccountInfoResponseDto responseDto = accountService.getAccountInfo(userId);

        return new ResponseEntity<>(new CustomResponseDto<>("조회 성공", responseDto), HttpStatus.OK);
    }

    /**
     * 계좌 입금을 요청한다.
     *
     * @param requestDto
     * @return
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestHeader("Authorization") String token, @RequestBody AccountRequestDto requestDto) {
        long userId = tokenService.extractUserId(token);
        int balance = accountService.deposit(userId, requestDto.getAmount());
        return new ResponseEntity<>(new CustomResponseDto<>("입금 성공", balance), HttpStatus.OK);
    }

    /**
     * 계좌 출금을 요청한다.
     *
     * @param requestDto
     * @return
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawal(@RequestHeader("Authorization") String token, @RequestBody AccountRequestDto requestDto) {
        long userId = tokenService.extractUserId(token);
        int balance = accountService.withdrawal(userId, requestDto.getAmount());

        return new ResponseEntity<>(new CustomResponseDto<>("출금 성공", balance), HttpStatus.OK);
    }

    /**
     * 계좌 송금을 요청한다.
     *
     * @param requestDto
     * @return
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String token, @RequestBody AccountRequestDto requestDto) {
        long userId = tokenService.extractUserId(token);
        accountService.transfer(userId, requestDto.getAccountNumber(), requestDto.getAmount());
        return new ResponseEntity<>(new CustomResponseDto<>("송금 성공", null), HttpStatus.OK);

    }
}
