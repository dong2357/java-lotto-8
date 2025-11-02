package lotto;

import camp.nextstep.edu.missionutils.Console;
import camp.nextstep.edu.missionutils.Randoms;

import java.util.Arrays;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;
import java.text.DecimalFormat;

public class Application {

    private static final int LOTTO_PRICE = 1000;
    private static final int LOTTO_NUMBER_MIN = 1;
    private static final int LOTTO_NUMBER_MAX = 45;
    private static final int LOTTO_NUMBER_COUNT = 6;
    private static final String ERROR_PREFIX = "[ERROR] ";

    public enum LottoRank {
        FIRST(6, 2000_000_000, "6개 일치"),
        SECOND(5, 30_000_000, "5개 일치, 보너스 볼 일치"),
        THIRD(5, 1_500_000, "5개 일치"),
        FOURTH(4, 50_000, "4개 일치"),
        FIFTH(3, 5000, "3개 일치"),
        MISS(0, 0, "낙첨");

        private final int matchCount;
        private final int prizeMoney;
        private final String description;

        LottoRank(int matchCount, int prizeMoney, String description) {
            this.matchCount = matchCount;
            this.prizeMoney = prizeMoney;
            this.description = description;
        }

        public static LottoRank valueOf(int matchCount, boolean bonusMatch) {
            if (matchCount == 6) {
                return FIRST;
            }
            if (matchCount == 5 && bonusMatch) {
                return SECOND;
            }
            if (matchCount == 5) {
                return THIRD;
            }
            return Arrays.stream(values())
                    .filter(rank -> rank.matchCount == matchCount && rank.matchCount >= 3)
                    .findFirst()
                    .orElse(MISS);
        }

        public int getPrizeMoney() { return prizeMoney; }
        public String getDescription() { return description; }
    }


    public static void main(String[] args) {
        int purchaseAmount = readPurchaseAmount();
        int lottoCount = purchaseAmount / LOTTO_PRICE;
        List<Lotto> userLottos = purchaseLottos(lottoCount);

        printPurchsedLottos(userLottos);

        Lotto winningNumbers = readWinningNumbers();

        int bounsNumber = readBonusNumber(winningNumbers);

        Map<LottoRank, Integer> statistics = calculateStatistics(userLottos, winningNumbers, bounsNumber);

        printStatistics(statistics);

        double profitRate = calculateProfitRate(statistics, purchaseAmount);
        printProfitRate(profitRate);
    }

    private static int readPurchaseAmount() {
        while(true) {
            try {
                System.out.println("구입금액을 입력해 주세요.");
                String input = Console.readLine();
                validatePurchaseAmount(input);
                return Integer.parseInt(input);
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
        }
    }

    private static void validatePurchaseAmount(String input) {
        int money;
        try{
            money = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + "숫자 형식의 금액을 입력해야 합니다.");
        }
        if (money<= 0 || money % LOTTO_PRICE != 0){
            throw new IllegalArgumentException(ERROR_PREFIX + "구입 금액은 1,000원 단위여야 합니다.");
        }
    }

    private static List<Lotto> purchaseLottos(int count) {
        return java.util.stream.Stream.generate(Application::generateLotto)
                .limit(count)
                .collect(java.util.stream.Collectors.toList());
    }
    private  static Lotto generateLotto() {
        List<Integer> numbers = Randoms.pickUniqueNumbersInRange(
                LOTTO_NUMBER_MIN,
                LOTTO_NUMBER_MAX,
                LOTTO_NUMBER_COUNT
        );
        return new Lotto(numbers);
    }

    private static void printPurchsedLottos(List<Lotto> lottos) {
        System.out.printf("\n%d개를 구매했습니다.\n", lottos.size());
        for (Lotto lotto : lottos) {
            System.out.println(lotto.getSortedNumbers());
        }
    }

    private static Lotto readWinningNumbers() {
        while(true) {
            try {
                System.out.println("\n당첨 번호를 입력해 주세요.");
                String input = Console.readLine();
                List<Integer> numbers = parseWinningNumbers(input);
                return new Lotto(numbers);
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
        }
    }

    private static List<Integer> parseWinningNumbers(String input) {
        try {
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(java.util.stream.Collectors.toList());
        } catch (NumberFormatException e){
            throw new IllegalArgumentException(ERROR_PREFIX + "숫자 형식의 당첨 번호를 입력해야 합니다.");
        }
    }

    private static int readBonusNumber(Lotto winningNumbers) {
        while(true){
            try{
                System.out.println("\n보너스 번호를 입력해 주세요.");
                String input = Console.readLine();
                int bounsNumber = parseBonusNumber(input);
                validateBounsNumber(winningNumbers, bounsNumber);
                return bounsNumber;
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
        }
    }

    private static int parseBonusNumber(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + "숫자 1개만 입력해야 합니다.");
        }
    }

    private static void validateBounsNumber(Lotto winningNumbrs, int bounsNumber) {
        if (bounsNumber < LOTTO_NUMBER_MIN || bounsNumber > LOTTO_NUMBER_MAX) {
            throw new IllegalArgumentException(ERROR_PREFIX + "로또 번호는 1부터 45 사이여야 합니다.");
        }
        if (winningNumbrs.contains(bounsNumber)) {
            throw new IllegalArgumentException(ERROR_PREFIX + "보너스 번호는 당첨 번호와 중복될 수 없습니다.");
        }
    }

    private static Map<LottoRank, Integer> calculateStatistics(
            List<Lotto> userLottos, Lotto winningNumbers, int bounsNumber) {

        Map<LottoRank, Integer> stats = new EnumMap<>(LottoRank.class);
        for (LottoRank rank : LottoRank.values()) {
            stats.put(rank, 0);
        }

        for (Lotto lotto : userLottos) {
            LottoRank rank = checkLottoRank(lotto, winningNumbers, bounsNumber);
            stats.put(rank, stats.get(rank) + 1);
        }
        return stats;
    }

    private static LottoRank checkLottoRank(Lotto lotto, Lotto winningNumbers, int bounsNumber) {
        int matchCount = lotto.countMatchingNumbers(winningNumbers);
        boolean bounsMatch = lotto.contains(bounsNumber);

        return LottoRank.valueOf(matchCount, bounsMatch);
    }

    private static void printStatistics(Map<LottoRank, Integer> statistics) {
        DecimalFormat moneyFormat = new DecimalFormat("###,###");
        System.out.println("\n당첨 통계\n---");

        List<LottoRank> ranksToShow = List.of(
                LottoRank.FIFTH, LottoRank.FOURTH, LottoRank.THIRD,
                LottoRank.SECOND, LottoRank.FIRST
        );

        for (LottoRank rank : ranksToShow) {
            System.out.printf("%s (%s원) - %d개\n",
                    rank.getDescription(),
                    moneyFormat.format(rank.getPrizeMoney()),
                    statistics.get(rank));
        }
    }

    private static double calculateProfitRate(Map<LottoRank, Integer> statistics, int purchaseAmount) {
        long totalPrize = 0;
        for (LottoRank rank : statistics.keySet()) {
            totalPrize += (long) rank.getPrizeMoney() * statistics.get(rank);
        }

        if (purchaseAmount == 0){
            return 0.0;
        }

        return ((double) totalPrize / purchaseAmount) * 100.0;
    }

    private static void printProfitRate(double profitRate) {
        System.out.printf("총 수익률은 %.1f%%입니다.\n", profitRate);
    }
}

