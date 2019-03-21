FROM java:7
ENV SCALA_VERSION 2.11.5
ENV SCALA_TARBALL http://www.scala-lang.org/files/archive/scala-$SCALA_VERSION.deb


RUN \
    echo "==> Install curl helper tool..."  && \
    apt-get update  && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --force-yes curl  && \
    \
    \
    \
    echo "===> install Scala"  && \
    DEBIAN_FRONTEND=noninteractive \
        apt-get install -y --force-yes libjansi-java  && \
    curl -sSL $SCALA_TARBALL -o scala.deb             && \
    dpkg -i scala.deb                                 && \
    \
    \
    \
    echo "===> clean up..."  && \
    rm -f *.deb                            && \
    apt-get remove -y --auto-remove curl   && \
    apt-get clean                          && \
    rm -rf /var/lib/apt/lists/*

COPY docker-from-file_2.11-0.1.0.jar /root/docker-from-file_2.11-0.1.0.jar

CMD ["scala", "-cp", "/root/docker-from-file_2.11-0.1.0.jar", "simple.Hello"]
