/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
 * Sweden. All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 * 
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */

package com.cinnober.exercise.ordermatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Order book with continuous matching of limit orders with time priority.
 *
 * <p>In an electronic exchange an order book is kept: All
 * buy and sell orders are entered into this order book and the prices are
 * set according to specific rules. Bids and asks are matched and trades
 * occur.

 * <p>This class keeps an order book, that can determine in real-time the
 * current market price and combine matching orders to trades. Each order
 * has a quantity and a price.
 *
 * <p><b>The trading rules:</b>
 * It is a match if a buy order exist at a higher price or equal to a sell
 * order in the order book. The quantity of both orders is reduced as much as
 * possible. When an order has a quantity of zero it is removed. An order can
 * match several other orders if the quantity is large enough and the price is
 * correct. The price of the trade is computed as the order that was in the
 * order book first (the passive party).
 *
 * <p>The priority of the orders to match is based on the following:
 * <ol>
 * <li> On the price that is best for the active order (the one just entered)
 * <li> On the time the order was entered (first come first served)
 * </ol>
 *
 * <p><b>Note:</b> some methods are not yet implemented. This is your job!
 * See {@link #addOrder(Order)} and {@link #getOrders(Side)}.
 */
public class OrderMatcher {


    private final LinkedList<Order> buyOrders = new LinkedList<>();
    private final LinkedList<Order> sellOrders = new LinkedList<>();

    /**
     * Create a new order matcher.
     */
    public OrderMatcher() {
    }

    /**
     * Add the specified order to the order book.
     *
     * @param order the order to be added, not null. The order will not be modified by the caller after this call.
     * @return any trades that were created by this order, not null.
     */
    public List<Trade> addOrder(Order order) {
        if (order.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedList<Order> oppositeOrders;
        LinkedList<Order> sameOrders;
        switch (order.getSide()) {
            case BUY:
                sameOrders = buyOrders;
                oppositeOrders = sellOrders;
                break;
            case SELL:
                sameOrders = sellOrders;
                oppositeOrders = buyOrders;
                break;
            default:
                throw new IllegalArgumentException("Illegal side");
        }

        List<Trade> trades = new LinkedList<>();
        // TODO: match against opposite side
        if (order.isEmpty()) {
            return trades;
        }

        // put order into the order book
        // TODO: keep the order list sorted, insert it into the right place!
        sameOrders.add(order);
        return trades;
    }

    /**
     * Create a trade between the orders. 
     * As much quantity as possible is traded. 
     * The order quantities is decreased, and at least one of the orders will become empty.
     * 
     * @param activeOrder the active order, not null
     * @param passiveOrder the passive order, not null.
     * @return 
     */
    private Trade trade(Order activeOrder, Order passiveOrder) {
        long qty = Math.min(activeOrder.getQuantity(), passiveOrder.getQuantity());
        activeOrder.decreaseQuantity(qty);
        passiveOrder.decreaseQuantity(qty);
        return new Trade(activeOrder.getId(), passiveOrder.getId(), passiveOrder.getPrice(), qty);
    }

    /**
     * Returns all remaining orders in the order book, in priority order, for the specified side.
     *
     * <p>Priority for buy orders is defined as highest price, followed by time priority (first come, first served).
     * For sell orders lowest price comes first, followed by time priority (same as for buy orders).
     *
     * @param side the side, not null.
     * @return all remaining orders in the order book, in priority order, for the specified side, not null.
     */
    public List<Order> getOrders(Side side) {
        switch(side) {
            case BUY:
                return buyOrders;
            case SELL:
                return sellOrders;
            default:
                throw new IllegalArgumentException();
        }
    }



    public static void main(String... args) throws Exception {
        OrderMatcher matcher = new OrderMatcher();
        System.out.println("Welcome to the order matcher. Type 'help' for a list of commands.");
        System.out.println();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        LOOP: while ((line=reader.readLine()) != null) {
            line = line.trim();
            try {
                switch(line) {
                    case "help":
                        System.out.println("Available commands: \n"
                                + "  buy|sell <quantity>@<price> [#<id>]  - Enter an order.\n"
                                + "  list                                 - List all remaining orders.\n"
                                + "  quit                                 - Quit.\n"
                                + "  help                                 - Show help (this message).\n");
                        break;
                    case "":
                        // ignore
                        break;
                    case "quit":
                        break LOOP;
                    case "list":
                        System.out.println("BUY:");
                        matcher.getOrders(Side.BUY).stream().map(Order::toString).forEach(System.out::println);
                        System.out.println("SELL:");
                        matcher.getOrders(Side.SELL).stream().map(Order::toString).forEach(System.out::println);
                        break;
                    default: // order
                        matcher.addOrder(Order.parse(line)).stream().map(Trade::toString).forEach(System.out::println);
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Bad input: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                System.err.println("Sorry, this command is not supported yet: " + e.getMessage());
            }
        }
        System.out.println("Good bye!");
    }
}
