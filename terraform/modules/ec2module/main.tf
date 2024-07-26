

data "aws_eip" "eip" {

  tags = {
    Name = var.eip_name
  }
}

resource "aws_instance" "myec2" {
  ami           = "ami-067ec4a660257c294"
  instance_type = var.instance_type
  key_name      = "devops-cicd-jenkins"
  tags = var.aws_common_tag
  security_groups = [aws_security_group.allow_http_https.name]
  root_block_device {
    delete_on_termination = true
    volume_size           = 100
    encrypted             = true
  }
}

resource "aws_eip_association" "eip_assoc" {
  instance_id   = aws_instance.myec2.id
  allocation_id = data.aws_eip.eip.id
}

resource "aws_security_group" "allow_http_https" {
  name = var.sg_name
  description = "Allow http and https inbound traffic"

  ingress {
    description = "TLS from VPC"
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
  }

  ingress {
    description = "http from VPC"
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
  }

  ingress {
    description = "http from VPC"
    from_port = 8080
    to_port = 8080
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
  }

  ingress {
    description = "ssh from VPC"
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
  }

  egress {
    description = "ssh from VPC"
    from_port = 0
    to_port = 0
    protocol = "ALL"
    cidr_blocks = [ "0.0.0.0/0" ]
  }
}
