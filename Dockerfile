FROM clojure:openjdk-8-lein-2.9.3-slim-buster

# RUN apt-get update -qq \
#     && echo "for compiling native gems etc" \
#     && apt-get install -y build-essential \
#     && echo "for nokogiri" \
#     && apt-get install -y libxml2-dev libxslt1-dev \
#     && echo "for postgres" \
#     && apt-get install -y libpq-dev

# # nodejs - TODO: include in base image
# RUN apt-get update -qq \
#     && apt-get install curl gnupg -yq \
#     && curl -sL https://deb.nodesource.com/setup_12.x | bash \
#     && apt-get install nodejs -yq

ENV APP_HOME /madek/server/api
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

# # ruby gems
# COPY Gemfile* ./
# COPY datalayer/Gemfile* ./datalayer/
# RUN bundle install

# lein deps
COPY project.clj ./
RUN lein deps

# application code and all the rest
COPY . ./

# build uberjar
RUN lein uberjar

# run uberjar
EXPOSE 3100
CMD ["java", "-jar", "target/api.jar"]

# alternative:
# RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
# CMD ["java", "-jar", "app-standalone.jar"]