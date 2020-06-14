/*
 * Copyright Ben Mullen 2016
 * All Rights Reserved
 * 
 */
package com.ben.java.brunch.BrunchCore.persistence;

import com.ben.java.brunch.BrunchCore.model.Item;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 *
 * @author Ben Mullen
 */
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

}