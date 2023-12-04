package com.crio.warmup.stock.portfolio;




import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class PortfolioManagerImpl implements PortfolioManager {
  private final RestTemplate restTemplate;




  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (PortfolioTrade trade : portfolioTrades) {
      try {
        List<Candle> candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);

        if (candles != null && !candles.isEmpty()) {
          Double buyPrice = candles.get(0).getOpen();
          Double sellPrice = candles.get(candles.size() - 1).getClose();
          LocalDate purchaseDate = trade.getPurchaseDate();
          long daysBetween = ChronoUnit.DAYS.between(purchaseDate, endDate);

          if (daysBetween == 0) {
            throw new IllegalArgumentException("The investment should be held for at least one day.");
          }

          if (buyPrice == 0) {
            throw new IllegalArgumentException("Buy price cannot be zero.");
          }

          double years = (double) daysBetween / 365;
          double annualizedReturn = Math.pow((sellPrice / buyPrice), 1 / years) - 1;

          AnnualizedReturn annualizedReturnObject = new AnnualizedReturn(
              trade.getSymbol(), annualizedReturn, annualizedReturn * 100);
          annualizedReturns.add(annualizedReturnObject);
        }
      } catch (JsonProcessingException e) {
        e.printStackTrace(); // Handle the exception based on your application's requirements
      }
    }

    annualizedReturns.sort(getComparator());
    return annualizedReturns;
  }

  // Extracted logic to call Tiingo third-party APIs
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    String uri = buildUri(symbol, from, to);
    String response = restTemplate.getForObject(uri, String.class);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    TiingoCandle[] tiingoCandles = mapper.readValue(response, TiingoCandle[].class);

    return Arrays.asList(tiingoCandles);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    // Build the Tiingo API URI using UriComponentsBuilder
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/{symbol}/prices";
    return UriComponentsBuilder.fromUriString(uriTemplate)
        .queryParam("startDate", startDate)
        .queryParam("endDate", endDate)
        .queryParam("token", "6503922a2bbd406cfc25fc7f4cf40e4af2a5a2d3")  // Replace with your actual Tiingo API key
        .buildAndExpand(symbol)
        .toUriString();
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }
}
