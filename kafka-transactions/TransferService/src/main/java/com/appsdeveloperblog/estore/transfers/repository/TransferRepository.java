package com.appsdeveloperblog.estore.transfers.repository;

import com.appsdeveloperblog.estore.transfers.model.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, String> {
}
