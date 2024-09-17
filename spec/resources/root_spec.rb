require "spec_helper"

describe "The root resource" do
  describe "the json_roa_client" do
    it "requesting succeeds" do
      expect { json_roa_client.get }.not_to raise_exception
    end

    describe "the response" do
      it "is a JSON_ROA::Client::Resource" do
        expect(json_roa_client.get).to be_a JSON_ROA::Client::Resource
      end
    end

    describe "the response/resource" do
      let :response do
        json_roa_client.get
      end

      describe "the data" do
        let :data do
          response.data
        end
        it "contains a 'api-version' key" do
          expect(data.keys).to include "api-version"
        end
      end

      describe "relations" do
        describe "'media-entries'" do
          it "is a JSON_ROA::Client::Relation" do
            expect(response.relation("media-entries")).to \
              be_a JSON_ROA::Client::Relation
          end
        end
        describe "'media-entry'" do
          it "is a JSON_ROA::Client::Relation" do
            expect(response.relation("media-entry")).to \
              be_a JSON_ROA::Client::Relation
          end
        end
      end
    end
  end
end
