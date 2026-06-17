package redis

import (
	"context"
	"fmt"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/go-redis/redis/v8"
)

// Client wraps go-redis to provide a Spring-like API.
type Client struct {
	client *redis.Client
}

// New creates a new Redis client.
func New(cfg *config.RedisConfig) *Client {
	rdb := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%s", cfg.Host, cfg.Port),
		Password: cfg.Password,
		DB:       cfg.DB,
	})
	return &Client{client: rdb}
}

// Ping checks the connection.
func (c *Client) Ping(ctx context.Context) error {
	return c.client.Ping(ctx).Err()
}

// Set stores a value without expiration.
func (c *Client) Set(ctx context.Context, key string, value any) error {
	return c.client.Set(ctx, key, value, 0).Err()
}

// SetEX stores a value with expiration.
func (c *Client) SetEX(ctx context.Context, key string, value any, ttl time.Duration) error {
	return c.client.Set(ctx, key, value, ttl).Err()
}

// GetString returns a string value.
func (c *Client) GetString(ctx context.Context, key string) (string, error) {
	return c.client.Get(ctx, key).Result()
}

// GetInt64 returns an int64 value, or 0 if missing.
func (c *Client) GetInt64(ctx context.Context, key string) (int64, error) {
	return c.client.Get(ctx, key).Int64()
}

// Delete removes keys.
func (c *Client) Delete(ctx context.Context, keys ...string) error {
	return c.client.Del(ctx, keys...).Err()
}

// Exists reports whether a key exists.
func (c *Client) Exists(ctx context.Context, key string) (bool, error) {
	n, err := c.client.Exists(ctx, key).Result()
	return n > 0, err
}

// SetNX sets a key only if it does not exist, with expiration.
func (c *Client) SetNX(ctx context.Context, key string, value any, ttl time.Duration) (bool, error) {
	return c.client.SetNX(ctx, key, value, ttl).Result()
}

// Increment increments a key by 1.
func (c *Client) Increment(ctx context.Context, key string) (int64, error) {
	return c.client.Incr(ctx, key).Result()
}

// IncrementBy increments a key by delta.
func (c *Client) IncrementBy(ctx context.Context, key string, delta int64) (int64, error) {
	return c.client.IncrBy(ctx, key, delta).Result()
}

// GetSet atomically sets a new value and returns the old value as int64.
func (c *Client) GetSet(ctx context.Context, key string, value any) (int64, error) {
	old, err := c.client.GetSet(ctx, key, value).Result()
	if err == redis.Nil {
		return 0, nil
	}
	if err != nil {
		return 0, err
	}
	var v int64
	_, err = fmt.Sscanf(old, "%d", &v)
	if err != nil {
		return 0, nil
	}
	return v, nil
}

// Expire sets expiration on a key.
func (c *Client) Expire(ctx context.Context, key string, ttl time.Duration) error {
	return c.client.Expire(ctx, key, ttl).Err()
}

// Keys returns keys matching a pattern.
func (c *Client) Keys(ctx context.Context, pattern string) ([]string, error) {
	return c.client.Keys(ctx, pattern).Result()
}

// ZIncrBy increments the score of a member in a sorted set.
func (c *Client) ZIncrBy(ctx context.Context, key string, increment float64, member string) (float64, error) {
	return c.client.ZIncrBy(ctx, key, increment, member).Result()
}

// ZRevRange returns the top N members of a sorted set in reverse order.
func (c *Client) ZRevRange(ctx context.Context, key string, start, stop int64) ([]string, error) {
	return c.client.ZRevRange(ctx, key, start, stop).Result()
}

// ZRevRangeWithScores returns members with scores.
func (c *Client) ZRevRangeWithScores(ctx context.Context, key string, start, stop int64) ([]redis.Z, error) {
	return c.client.ZRevRangeWithScores(ctx, key, start, stop).Result()
}

// SAdd adds members to a set.
func (c *Client) SAdd(ctx context.Context, key string, members ...string) error {
	return c.client.SAdd(ctx, key, members).Err()
}

// SMembers returns all members of a set.
func (c *Client) SMembers(ctx context.Context, key string) ([]string, error) {
	return c.client.SMembers(ctx, key).Result()
}

// Close closes the client.
func (c *Client) Close() error {
	return c.client.Close()
}
