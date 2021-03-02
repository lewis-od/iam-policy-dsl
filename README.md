# IAM Policy DSL
A Kotlin DSL for creating AWS IAM Policy documents

## Usage
Add to your Gradle dependencies as:
```kotlin
implementation("com.github.lewis-od:iam-policy-dsl:1.1")
```

The DSL can then be used like:
```kotlin
val myPolicy: Policy = policy {
    statement("EC2FullAccess") {
        effect(ALLOW)
        action("ec2:*")
        resource("*")
    }
    statement("S3ProdAccess") {
        effect(ALLOW)
        action("s3:ListObjects")
        action("s3:GetObject")
        resource("arn:aws:s3:::prod-bucket")
    }
}
val policyDocument: String = myPolicy.toJson()
```

## Examples

### Identity-Based Policy
```kotlin
policy {
    statement("EC2FullAccess") {
        effect(ALLOW)
        action("ec2:*")
        resource("*")
    }
    statement("S3ProdAccess") {
        effect(ALLOW)
        action("s3:ListObjects", "s3:GetObject")
        resource("arn:aws:s3:::prod-bucket")
    }
}
```

Corresponds to:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "EC2FullAccess",
            "Effect": "Allow",
            "Action": [
                "ec2:*"
            ],
            "Resource": "*"
        },
        {
            "Sid": "S3ProdAccess",
            "Effect": "Allow",
            "Action": [
                "s3:ListObjects",
                "s3:GetObject"
            ],
            "Resource": "arn:aws:s3:::prod-bucket"
        }
    ]
}
```

### Resource-Based Policy
```kotlin
policy("2008-10-17") {
    statement("AllowAccess") {
        effect(ALLOW)
        action("sts:AssumeRole")
        principal {
            aws("arn:aws:iam::123456789123:root", 
                "arn:aws:iam::456789012345:root")
        }
        action("ecr:BatchCheckLayerAvailability", 
               "ecr:BatchGetImage",
               "ecr:GetDownloadUrlForLayer")
    }
}
```

Corresponds to:
```json
{
  "Version": "2008-10-17",
  "Statement": [
    {
      "Sid": "AllowAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": [
          "arn:aws:iam::123456789123:root",
          "arn:aws:iam::456789012345:root"
        ]
      },
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:BatchGetImage",
        "ecr:GetDownloadUrlForLayer"
      ]
    }
  ]
}
```

### Trust Policy
```kotlin
policy {
    statement("AllowAssumeRole") {
        effect(ALLOW)
        action("sts:AssumeRole")
        principal {
            aws("arn:aws:iam::000000000000:user/user-name")
        }
    }
}
```

Corresponds to:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowAssumeRole",
            "Effect": "Allow",
            "Action": ["sts:AssumeRole"],
            "Principal": { "AWS": "arn:aws:iam::000000000000:user/user-name" }
        }
    ]
}
```
