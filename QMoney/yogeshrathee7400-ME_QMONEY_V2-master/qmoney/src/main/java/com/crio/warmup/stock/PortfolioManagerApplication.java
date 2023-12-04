package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;



import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.ResourceUtils;

public class PortfolioManagerApplication {
  private static final int DAYS_IN_YEAR = 365;
  private static final String API_TOKEN = "6503922a2bbd406cfc25fc7f4cf40e4af2a5a2d3";

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    if (args.length != 1) {
      throw new IllegalArgumentException("trades.json");
    }
    String filename = args[0];
    ObjectMapper objectMapper = getObjectMapper();
    List<String> symbols = new ArrayList<>();

    try {
      File file = ResourceUtils.getFile("classpath:" + filename);
      List<PortfolioTrade> portfolioTrades = objectMapper.readValue(file, objectMapper.getTypeFactory()
          .constructCollectionType(List.class, PortfolioTrade.class));

      for (PortfolioTrade trade : portfolioTrades) {
        symbols.add(trade.getSymbol());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return symbols;
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    if (args.length < 2) {
        throw new IllegalArgumentException("Insufficient arguments. Usage: <filename> <endDate>");
    }

    String filename = args[0];
    LocalDate endDate;

    try {
      endDate = LocalDate.parse(args[1]);
    } catch (DateTimeParseException e) {
      throw new RuntimeException("Invalid end date format. Please use yyyy-MM-dd format.", e);
    }

    List<PortfolioTrade> trades = readTradesFromJson(filename);

    List<TotalReturnsDto> totalReturns = new ArrayList<>();
    String tiingoToken = "6503922a2bbd406cfc25fc7f4cf40e4af2a5a2d3";
    RestTemplate restTemplate = new RestTemplate();

    for (PortfolioTrade trade : trades) {
        String apiUrl = prepareUrl(trade, endDate, tiingoToken);
        TiingoCandle[] candles = restTemplate.getForObject(apiUrl, TiingoCandle[].class);

        if (candles != null && candles.length > 0) {
            double closingPrice = candles[candles.length - 1].getClose();
            totalReturns.add(new TotalReturnsDto(trade.getSymbol(), closingPrice));
        }
    }

    totalReturns.sort(Comparator.comparingDouble(TotalReturnsDto::getClosingPrice));
    List<String> sortedSymbols = totalReturns.stream().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());
    return sortedSymbols;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    return Arrays.asList(objectMapper.readValue(file, PortfolioTrade[].class));
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String baseUrl = " ";
    try {
      String symbol = trade.getSymbol();
      String startDate = trade.getPurchaseDate().toString();
      String endDateStr = endDate.toString();
      if(endDateStr.compareTo(startDate)>0){
        baseUrl =   UriComponentsBuilder.fromHttpUrl("https://api.tiingo.com/tiingo/daily/")
        .pathSegment(symbol, "prices")
        .queryParam("startDate", startDate)
        .queryParam("endDate", endDateStr)
        .queryParam("token", token)
        .toUriString();
      }
    } catch (DateTimeParseException e) {
      throw new RuntimeException("Invalid end date format. Please use yyyy-MM-dd format.", e);
    }
    return baseUrl;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    List<String> symbols = mainReadFile(args);
    List<String> sortedSymbols = mainReadQuotes(args);

    System.out.println("Portfolio Symbols: " + symbols);
    System.out.println("Sorted Symbols: " + sortedSymbols);

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }

  public static void printJsonObject(List<AnnualizedReturn> returns) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(returns);
      System.out.println(json);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    long daysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
  
    if (daysBetween == 0) {
        throw new IllegalArgumentException("The investment should be held for at least one day.");
    }
  
    if (buyPrice == 0) {
        throw new IllegalArgumentException("Buy price cannot be zero.");
    }
  
    double years = (double) daysBetween / DAYS_IN_YEAR;
    double annualizedReturn = Math.pow((sellPrice / buyPrice), 1 / years) - 1;
  
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, annualizedReturn * 100);
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "trades.json";
    String toStringOfObjectMapper = "ObjectMapper";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "";

    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {
    if (args.length != 2) {
        throw new IllegalArgumentException("Usage: <filename> <endDate>");
    }

    String filename = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(filename);
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (PortfolioTrade trade : portfolioTrades) {
        List<Candle> candles = fetchCandles(trade, endDate, getToken());

        if (candles != null) {
            AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(candles.get(candles.size()-1).getDate(),trade,candles.get(0).getOpen(), candles.get(candles.size()-1).getClose());
            annualizedReturns.add(annualizedReturn);
        }
    }

    Collections.sort(annualizedReturns, (a, b) -> Double.compare(b.getAnnualizedReturn(), a.getAnnualizedReturn()));
    return annualizedReturns;
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if (candles != null && !candles.isEmpty()) {
        return candles.get(0).getOpen();
    }
    return null;
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    if (candles != null && !candles.isEmpty()) {
        return candles.get(candles.size() - 1).getClose();
    }
    return null;
  }

  public static Object getToken() {
    return API_TOKEN;
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, Object token) {
    List<Candle> candles = new ArrayList<>();

    String apiUrl = prepareUrl(trade, endDate, (String) token);

    try {
        RestTemplate restTemplate = new RestTemplate();
        TiingoCandle[] tiingoCandles = restTemplate.getForObject(apiUrl, TiingoCandle[].class);

        if (tiingoCandles != null) {
            candles = Arrays.asList(tiingoCandles);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return candles;
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args) throws Exception {
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    String contents = readFileAsString(file);
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> portfolioTrades = objectMapper.readValue(contents, objectMapper.getTypeFactory()
        .constructCollectionType(List.class, PortfolioTrade.class));

    // Get the RestTemplate instance
    RestTemplate restTemplate = new RestTemplate();

    // Get the PortfolioManager instance using the factory
    PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

    // Calculate annualized returns
    return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  private static String readFileAsString(String file) throws IOException {
    return new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
  }
}



