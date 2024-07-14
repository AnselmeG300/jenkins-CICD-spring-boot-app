variable "instance_type" {
  type = string
  description = "set aws instance type"
  default = "t2.nano"
}

variable "sg_name" {
  type = string
  description = "set sg name"
  default = "eazy-sg"
}

variable "eip_name" {
  type = string
  description = "set eip name"
  default = "prod-jenkins"
}

variable "aws_common_tag" {
  type = map
  description = "Set aws tag"
  default = {
    Name = "ec2-eazytraining"
  }
}