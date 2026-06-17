package crypto

import "testing"

func TestHashAndCheck(t *testing.T) {
	password := "secret123"
	hash, err := HashPassword(password)
	if err != nil {
		t.Fatalf("hash failed: %v", err)
	}
	if !CheckPassword(password, hash) {
		t.Fatal("password should match")
	}
	if CheckPassword("wrong", hash) {
		t.Fatal("wrong password should not match")
	}
}
