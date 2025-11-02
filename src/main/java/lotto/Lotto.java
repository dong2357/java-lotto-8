package lotto;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Lotto {
    private final List<Integer> numbers;

    public Lotto(List<Integer> numbers) {
        validate(numbers);
        this.numbers = numbers;
    }

    private void validate(List<Integer> numbers) {
        if (numbers.size() != 6) {
            throw new IllegalArgumentException("[ERROR] 로또 번호는 6개여야 합니다.");
        }
        if (hasDuplicates(numbers)) {
            throw new IllegalArgumentException("[ERROR] 로또 번호에 중복된 숫자가 있습니다.");
        }
        if (isOutOfRange(numbers)) {
            throw new IllegalArgumentException("[ERROR] 로또 번호는 1부터 45 사이의 숫자여야 합니다.");
        }
    }

    // 중복 검사
    private boolean hasDuplicates(List<Integer> numbers) {
        Set<Integer> uniqueNumbers = new HashSet<>(numbers);
        return uniqueNumbers.size() != numbers.size();
    }

    // 범위 검사 (1~45)
    private boolean isOutOfRange(List<Integer> numbers) {
        return numbers.stream().anyMatch(number -> number < 1 || number > 45);
    }

    // [추가 기능] 정렬된 번호 반환 (출력용)
    public List<Integer> getSortedNumbers() {
        // 원본 리스트(this.numbers)는 불변이어야 하므로 정렬된 새 리스트를 반환
        return numbers.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    // [추가 기능] 특정 번호를 포함하는지 확인 (비교용)
    public boolean contains(int number) {
        return numbers.contains(number);
    }

    // [추가 기능] 다른 로또와 몇 개가 일치하는지 계산
    public int countMatchingNumbers(Lotto otherLotto) {
        return (int) numbers.stream()
                .filter(otherLotto::contains) // otherLotto.contains(number)
                .count();
    }
}