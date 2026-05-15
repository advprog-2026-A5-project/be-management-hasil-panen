variable "project_name" {
  type    = string
  default = "hasilpanen"
}

variable "aws_region" {
  type    = string
  default = "ap-southeast-1"
}

variable "vpc_cidr" {
  type    = string
  default = "10.20.0.0/16"
}

variable "public_subnet_cidr_a" {
  type    = string
  default = "10.20.1.0/24"
}

variable "public_subnet_cidr_b" {
  type    = string
  default = "10.20.2.0/24"
}

variable "availability_zone_a" {
  type    = string
  default = "ap-southeast-1a"
}

variable "availability_zone_b" {
  type    = string
  default = "ap-southeast-1b"
}

variable "alb_ingress_cidr" {
  type    = string
  default = "0.0.0.0/0"
}

variable "rds_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "rds_allocated_storage" {
  type    = number
  default = 20
}

variable "db_name" {
  type    = string
  default = "hasilpanen_db"
}

variable "db_username" {
  type    = string
  default = "hasilpanen_user"
}

variable "db_password" {
  type      = string
  sensitive = true
}
