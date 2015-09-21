FROM videoamp/scala-sbt-base
MAINTAINER James Wu "james@dvideoamp.com"
LABEL name="quotient-filter" version="0.1" description="A probablistic data store for use with user data"
COPY . /quotient-filter
WORKDIR /quotient-filter
EXPOSE 80
RUN sbt exit
ENTRYPOINT ["sbt", "run"]
CMD ["> outfile"]
