package com.opensource.moneymanager.controller;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.mapper.TransactionMapper;
import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransactionDto> list() {
        return service.findAll().stream().map(TransactionMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> get(@PathVariable Long id) {
        return service.findById(id).map(TransactionMapper::toDto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(@RequestBody TransactionDto dto) {
        Transaction saved = service.save(TransactionMapper.toEntity(dto));
        TransactionDto out = TransactionMapper.toDto(saved);
        return ResponseEntity.created(URI.create("/api/transactions/" + out.getId())).body(out);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
