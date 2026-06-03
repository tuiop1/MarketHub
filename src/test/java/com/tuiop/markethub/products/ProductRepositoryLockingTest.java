package com.tuiop.markethub.products;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryLockingTest {

    @Test
    void findBuyableByIdsForUpdate_usesPessimisticWriteLockForStockConsistency() throws NoSuchMethodException {
        Method method = ProductRepository.class.getMethod("findBuyableByIdsForUpdate", Collection.class);

        Lock lock = method.getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
