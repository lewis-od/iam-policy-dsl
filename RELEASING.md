# Releasing
To release to maven central, first set the correct version number in [build.gradle.kts](build.gradle.kts), then create tag for the
release with:
```shell
git tag -s v1.0 -m 'Release 1.0'
```

Push the tag with `git push origin v1.0`. This will trigger the [release workflow], which will build the release, sign
it, and publish it to a Sonatype staging repo.

Finally go to [https://oss.sonatype.org/](https://oss.sonatype.org/), log in, click on "Staging Repositories", tick the 
new release, and click "Close". Once all the checks are complete, click "release" publish the release to Sonatype, 
which will be synced to maven central in ~15 minutes.

[release workflow]: https://github.com/lewis-od/iam-policy-dsl/actions?query=workflow%3A%22Release+to+maven+central%22
