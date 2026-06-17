package db

import (
	"fmt"
	"log/slog"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// New initializes a GORM MySQL connection.
func New(cfg *config.MySQLConfig) (*gorm.DB, error) {
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=utf8mb4&parseTime=True&loc=Asia%%2FShanghai",
		cfg.Username, cfg.Password, cfg.Host, cfg.Port, cfg.Database)

	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		return nil, fmt.Errorf("open mysql failed: %w", err)
	}

	sqlDB, err := db.DB()
	if err != nil {
		return nil, err
	}
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetMaxOpenConns(100)

	slog.Info("mysql connected", slog.String("database", cfg.Database))
	return db, nil
}

// AutoMigrate creates/updates tables for the application models.
// Fulltext indexes and initial schema are assumed to be handled by schema.sql or existing DB.
func AutoMigrate(db *gorm.DB) error {
	return db.AutoMigrate(
		&model.User{},
		&model.Article{},
		&model.ArticleContent{},
		&model.Comment{},
		&model.Tag{},
		&model.ArticleTag{},
		&model.UserLike{},
	)
}
