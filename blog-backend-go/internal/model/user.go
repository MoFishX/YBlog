package model

import "time"

// User maps to the `user` table.
type User struct {
	ID        int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	Username  string    `gorm:"column:username;size:50;not null;uniqueIndex:uk_username" json:"username"`
	Password  string    `gorm:"column:password;size:255;not null" json:"-"`
	Email     string    `gorm:"column:email;size:100" json:"email"`
	Avatar    string    `gorm:"column:avatar;size:255" json:"avatar"`
	Role      string    `gorm:"column:role;size:20;not null;default:USER" json:"role"`
	Status    string    `gorm:"column:status;size:20;not null;default:ACTIVE" json:"status"`
	CreatedAt time.Time `gorm:"column:created_at;not null" json:"createdAt"`
	UpdatedAt time.Time `gorm:"column:updated_at;not null" json:"updatedAt"`
}

func (User) TableName() string {
	return "user"
}
