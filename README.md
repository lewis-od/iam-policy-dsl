# IAM Policy DSL
A Kotlin DSL for creating AWS IAM Policy documents

## Usage
```kotlin
val myPolicy: Policy = policy("2012-10-17") {
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
`policyDocument` will have the value:
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

## ToDo
- [ ] Support specifying a `Principal` block in a statement
- [ ] Support conditional policies
- [ ] Support `NotAction`, `NotResource`, and `NotPrincipal`
