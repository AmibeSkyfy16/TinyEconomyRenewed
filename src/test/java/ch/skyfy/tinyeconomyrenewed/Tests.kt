package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.server.features.MarketPriceUpdaterFeature
import info.bitrich.xchangestream.binance.BinanceStreamingExchange
import info.bitrich.xchangestream.binancefuture.BinanceFutureStreamingExchange
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange
import info.bitrich.xchangestream.core.StreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.knowm.xchange.currency.CurrencyPair
import java.math.BigDecimal
import java.net.http.WebSocket
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class Tests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testWithMultipleDelays() = runTest {


    }

    private var lastPrice: Double = -1.0

    var currentDirtPrice = 50.0

    @Test
   fun test2(){

       var price = 12.0

        for(i in 0..10){
            val percentUp = 9
            price = (100.0 + (percentUp)) * price / 100.0
            println(price)
        }
   }

    @Test
    fun t(){
        if(0 == 0)return
        val exchange: StreamingExchange = StreamingExchangeFactory.INSTANCE.createExchange(BitfinexStreamingExchange::class.java)

        exchange.connect().blockingAwait()

        val sub = exchange.streamingMarketDataService.getTrades(CurrencyPair("NEO/USD")).subscribe {
            if(lastPrice.toInt() != -1){
                var p = it.price.toDouble() * 100 / lastPrice - 100
                p *= 2
//                val percent = it.price.multiply(100.toBigDecimal()).divide(lastPrice).minus(100.toBigDecimal())
                println("percent $p")

                println("currentDirtPrice $currentDirtPrice")
                currentDirtPrice = (100.0 + (p)) * currentDirtPrice / 100.0
                println("newP $currentDirtPrice")
            }

            println("price ${it.price}")
            lastPrice = it.price.toDouble()
        }


        Thread.sleep(120_000)
        sub.dispose()
        exchange.disconnect().blockingAwait();
    }

}