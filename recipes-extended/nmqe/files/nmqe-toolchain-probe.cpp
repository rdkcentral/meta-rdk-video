// SPDX-License-Identifier: MIT
#include <algorithm>
#include <array>
#include <concepts>
#include <iostream>
#include <span>
#include <thread>

static_assert(__cplusplus >= 202002L, "The MoQ toolchain probe requires C++20");

template <std::integral T>
constexpr T sum(std::span<const T> values)
{
    T total{};
    for (const T value : values)
        total += value;
    return total;
}

constexpr std::array<int, 4> values{1, 2, 3, 4};
static_assert(sum<int>(std::span<const int>(values)) == 10);

int main()
{
    int result = 0;
    std::thread worker([&result] {
        result = sum<int>(std::span<const int>(values));
    });
    worker.join();

    if (result != 10 || !std::ranges::all_of(values, [](int value) { return value > 0; }))
    {
        std::cerr << "NMQE C++20 toolchain probe FAILED\n";
        return 1;
    }

    std::cout << "NMQE C++20 toolchain probe PASS\n"
              << "Compiler: " << __VERSION__ << '\n'
              << "__cplusplus: " << __cplusplus << '\n'
              << "Checks: concepts, constexpr span, ranges, thread linking/runtime\n";
    return 0;
}
