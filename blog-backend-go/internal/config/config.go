package config

import (
	"log/slog"
	"strings"
	"time"

	"github.com/spf13/viper"
)

// AppConfig holds all application configurations.
type AppConfig struct {
	Server  ServerConfig
	MySQL   MySQLConfig
	Redis   RedisConfig
	JWT     JWTConfig
	AI      AIConfig
	Storage StorageConfig
	Email   EmailConfig
	App     AppEnvConfig
	Admin   AdminConfig
}

type ServerConfig struct {
	Port string
}

type MySQLConfig struct {
	Host     string
	Port     string
	Database string
	Username string
	Password string
}

type RedisConfig struct {
	Host     string
	Port     string
	Password string
	DB       int
}

type JWTConfig struct {
	Secret                   string
	AccessTokenExpiration    time.Duration
	RefreshTokenExpiration   time.Duration
}

type AIConfig struct {
	BaseURL     string
	APIKey      string
	Model       string
	MaxTokens   int
	Temperature float64
	Enabled     bool
}

type StorageConfig struct {
	Type  string
	Local LocalStorageConfig
	OSS   OSSConfig
}

type LocalStorageConfig struct {
	Path string
}

type OSSConfig struct {
	Endpoint   string
	AccessKey  string
	SecretKey  string
	BucketName string
	PathPrefix string
}

type EmailConfig struct {
	ResendAPIKey string
}

type AppEnvConfig struct {
	Domain      string
	FrontendURL string
}

type AdminConfig struct {
	Username string
	Password string
	Email    string
}

// Load reads configuration from environment variables.
func Load() *AppConfig {
	v := viper.New()
	v.SetEnvPrefix("")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	v.AutomaticEnv()

	cfg := &AppConfig{}

	cfg.Server.Port = v.GetString("SERVER_PORT")
	if cfg.Server.Port == "" {
		cfg.Server.Port = "1145"
	}

	cfg.MySQL.Host = v.GetString("MYSQL_HOST")
	if cfg.MySQL.Host == "" {
		cfg.MySQL.Host = "localhost"
	}
	cfg.MySQL.Port = v.GetString("MYSQL_PORT")
	if cfg.MySQL.Port == "" {
		cfg.MySQL.Port = "3306"
	}
	cfg.MySQL.Database = v.GetString("MYSQL_DATABASE")
	if cfg.MySQL.Database == "" {
		cfg.MySQL.Database = "yblog"
	}
	cfg.MySQL.Username = v.GetString("MYSQL_USERNAME")
	if cfg.MySQL.Username == "" {
		cfg.MySQL.Username = "root"
	}
	cfg.MySQL.Password = v.GetString("MYSQL_ROOT_PASSWORD")

	cfg.Redis.Host = v.GetString("REDIS_HOST")
	if cfg.Redis.Host == "" {
		cfg.Redis.Host = "localhost"
	}
	cfg.Redis.Port = v.GetString("REDIS_PORT")
	if cfg.Redis.Port == "" {
		cfg.Redis.Port = "6379"
	}
	cfg.Redis.Password = v.GetString("REDIS_PASSWORD")
	cfg.Redis.DB = v.GetInt("REDIS_DB")
	if cfg.Redis.DB == 0 && !v.IsSet("REDIS_DB") {
		cfg.Redis.DB = 1
	}

	cfg.JWT.Secret = v.GetString("JWT_SECRET")
	cfg.JWT.AccessTokenExpiration = time.Duration(v.GetInt("JWT_ACCESS_TOKEN_EXPIRATION")) * time.Second
	if cfg.JWT.AccessTokenExpiration == 0 {
		cfg.JWT.AccessTokenExpiration = 5 * time.Minute
	}
	cfg.JWT.RefreshTokenExpiration = time.Duration(v.GetInt("JWT_REFRESH_TOKEN_EXPIRATION")) * time.Second
	if cfg.JWT.RefreshTokenExpiration == 0 {
		cfg.JWT.RefreshTokenExpiration = 30 * 24 * time.Hour
	}

	cfg.AI.BaseURL = v.GetString("AI_BASE_URL")
	if cfg.AI.BaseURL == "" {
		cfg.AI.BaseURL = "https://api.deepseek.com"
	}
	cfg.AI.APIKey = v.GetString("AI_API_KEY")
	cfg.AI.Model = v.GetString("AI_MODEL")
	if cfg.AI.Model == "" {
		cfg.AI.Model = "deepseek-chat"
	}
	cfg.AI.MaxTokens = v.GetInt("AI_MAX_TOKENS")
	if cfg.AI.MaxTokens == 0 {
		cfg.AI.MaxTokens = 1024
	}
	cfg.AI.Temperature = v.GetFloat64("AI_TEMPERATURE")
	if cfg.AI.Temperature == 0 && !v.IsSet("AI_TEMPERATURE") {
		cfg.AI.Temperature = 0.3
	}
	cfg.AI.Enabled = v.GetBool("AI_ENABLED")
	if !v.IsSet("AI_ENABLED") {
		cfg.AI.Enabled = true
	}

	cfg.Storage.Type = v.GetString("STORAGE_TYPE")
	if cfg.Storage.Type == "" {
		cfg.Storage.Type = "local"
	}
	cfg.Storage.Local.Path = v.GetString("LOCAL_STORAGE_PATH")
	if cfg.Storage.Local.Path == "" {
		cfg.Storage.Local.Path = "/uploads"
	}
	cfg.Storage.OSS.Endpoint = v.GetString("OSS_QINIU_ENDPOINT")
	cfg.Storage.OSS.AccessKey = v.GetString("OSS_QINIU_ACCESS_KEY_ID")
	cfg.Storage.OSS.SecretKey = v.GetString("OSS_QINIU_ACCESS_KEY_SECRET")
	cfg.Storage.OSS.BucketName = v.GetString("OSS_QINIU_BUCKET_NAME")
	cfg.Storage.OSS.PathPrefix = v.GetString("OSS_QINIU_PATH_PREFIX")
	if cfg.Storage.OSS.PathPrefix == "" {
		cfg.Storage.OSS.PathPrefix = "yblog/uploads"
	}

	cfg.Email.ResendAPIKey = v.GetString("EMAIL_RESEND_API_KEY")

	cfg.App.Domain = v.GetString("APP_DOMAIN")
	cfg.App.FrontendURL = v.GetString("APP_URL")
	if cfg.App.FrontendURL == "" {
		cfg.App.FrontendURL = "http://localhost:5173"
	}

	cfg.Admin.Username = v.GetString("ADMIN_USERNAME")
	cfg.Admin.Password = v.GetString("ADMIN_PASSWORD")
	cfg.Admin.Email = v.GetString("ADMIN_EMAIL")

	slog.Info("config loaded",
		slog.String("mysql", cfg.MySQL.Host+":"+cfg.MySQL.Port),
		slog.String("redis", cfg.Redis.Host+":"+cfg.Redis.Port),
		slog.String("storage", cfg.Storage.Type),
	)
	return cfg
}
