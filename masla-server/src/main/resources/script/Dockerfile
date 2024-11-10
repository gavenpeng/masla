FROM ubuntu:latest
LABEL authors="pengrongxin"
COPY conf /masla/conf
COPY lib /masla/lib

RUN chmod +x /masal/run.sh
RUN chmod +x /masla/masla.sh

ENTRYPOINT ["/masla/run.sh start"]