from typing import List
import sys

BASE = 1 << 32
MASK = BASE - 1

class BigInt:
    def __init__(self, limbs: List[int]):
        self.limbs = limbs
        self._normalize()

    @classmethod
    def from_int(cls, v: int):
        if v == 0:
            return cls([0])
        limbs = []
        while v > 0:
            limbs.append(v & MASK)
            v >>= 32
        return cls(limbs)

    @classmethod
    def from_decimal_string(cls, s: str):
        if s == "0":
            return cls([0])
        digits = [int(ch) for ch in s.strip()]
        limbs = []
        while any(digits):
            carry = 0
            new_digits = []
            for d in digits:
                carry = carry * 10 + d
                q = carry // BASE
                r = carry % BASE
                if new_digits or q != 0:
                    new_digits.append(q)
                carry = r
            limbs.append(carry)
            digits = new_digits
        return cls(limbs)

    def _normalize(self):
        while len(self.limbs) > 1 and self.limbs[-1] == 0:
            self.limbs.pop()

    def to_int(self) -> int:
        v = 0
        for i in reversed(range(len(self.limbs))):
            v = (v << 32) | self.limbs[i]
        return v

    def to_hex(self) -> str:
        self._normalize()
        parts = []
        for limb in reversed(self.limbs):
            parts.append(f"{limb:08x}")
        hex_str = "".join(parts).lstrip("0")
        if hex_str == "":
            hex_str = "0"
        return hex_str

def big_mult(a: BigInt, b: BigInt) -> BigInt:
    a._normalize()
    b._normalize()
    n = len(a.limbs)
    m = len(b.limbs)
    res = [0] * (n + m)
    for i in range(n):
        ai = a.limbs[i]
        carry = 0
        for j in range(m):
            prod = ai * b.limbs[j]
            total = res[i + j] + prod + carry
            res[i + j] = total & MASK
            carry = total >> 32
        k = i + m
        while carry:
            total = res[k] + carry
            res[k] = total & MASK
            carry = total >> 32
            k += 1
    return BigInt(res)

def main():
    if len(sys.argv) == 1:
        x = 100_000
        y = 100_000
        A = BigInt.from_int(x)
        B = BigInt.from_int(y)
        P = big_mult(A, B)
        print(f"{x} x {y} = {P.to_int()}")
        print("hex:", P.to_hex())
    elif len(sys.argv) == 3:
        a_str = sys.argv[1]
        b_str = sys.argv[2]
        A = BigInt.from_decimal_string(a_str)
        B = BigInt.from_decimal_string(b_str)
        P = big_mult(A, B)
        print(f"{a_str} x {b_str} = {P.to_int()}")
        print("hex:", P.to_hex())
    else:
        print("Usage: python Big numbers calculation.py a b")

if __name__ == "__main__":
    main()
