require "spec_helper"
require "cgi"
require "uri"

describe "JSON-ROA pagination of media-entries" do
  let :media_entries_relation do
    json_roa_client.get.relation("media-entries")
  end

  def parse_query(href)
    CGI.parse(URI.parse(href).query.to_s)
  end

  def get_by_href(href)
    uri = URI.parse(href)
    # Relative ROA hrefs parse as URI::Generic (no request_uri).
    path = uri.query ? "#{uri.path}?#{uri.query}" : uri.path
    plain_faraday_json_client.get(path)
  end

  def create_public_media_entry!
    FactoryBot.create(:media_entry, get_metadata_and_previews: true)
  end

  context "without filter_by" do
    context "when results fit on one page" do
      let!(:created_ids) do
        5.times.map { create_public_media_entry!.id }
      end

      it "does not offer a next relation" do
        response = media_entries_relation.get
        expect(response.response.status).to be == 200
        expect(response.data["media-entries"].size).to be == 5
        expect(response.json_roa_data.fetch("collection")).not_to have_key("next")
      end
    end

    context "when results fill more than one page" do
      let!(:created_ids) do
        105.times.map { create_public_media_entry!.id }
      end

      it "offers a next relation without filter_by and returns the remaining entries" do
        first_page = media_entries_relation.get
        expect(first_page.response.status).to be == 200
        expect(first_page.data["media-entries"].size).to be == 100

        first_ids = first_page.data["media-entries"].map { |me| me["id"] }
        expect(Set.new(first_ids)).to be <= Set.new(created_ids)

        next_href = first_page.json_roa_data.fetch("collection").fetch("next").fetch("href")
        query = parse_query(next_href)
        expect(query).to have_key("page")
        expect(query["page"].first.to_i).to be == 1
        expect(query).not_to have_key("filter_by")

        second_page = get_by_href(next_href)
        expect(second_page.status).to be == 200
        second_ids = second_page.body["media-entries"].map { |me| me["id"] }
        expect(second_ids.size).to be == 5
        expect(Set.new(second_ids)).to be <= Set.new(created_ids)
        expect(Set.new(first_ids) & Set.new(second_ids)).to be_empty
        expect(Set.new(first_ids) | Set.new(second_ids)).to eq(Set.new(created_ids))
      end
    end
  end

  context "with filter_by" do
    let(:match_term) { "brot-filter-pager-961" }

    def create_matching_media_entry!
      media_entry = create_public_media_entry!
      meta_datum_text = FactoryBot.create(:meta_datum_text, value: match_term)
      media_entry.meta_data << meta_datum_text
      media_entry
    end

    def filter_params
      { "filter_by" => { meta_data: [{ key: "any", match: match_term }] }
        .deep_stringify_keys
        .to_json }
    end

    context "when filtered results fit on one page" do
      before do
        5.times { create_matching_media_entry! }
        10.times { create_public_media_entry! }
      end

      it "does not offer a next relation" do
        response = media_entries_relation.get(filter_params)
        expect(response.response.status).to be == 200
        expect(response.data["media-entries"].size).to be == 5
        expect(response.json_roa_data.fetch("collection")).not_to have_key("next")
      end
    end

    context "when filtered results fill a full page" do
      before do
        100.times { create_matching_media_entry! }
        20.times { create_public_media_entry! }
      end

      it "preserves JSON filter_by on the next href and keeps filtering" do
        first_page = media_entries_relation.get(filter_params)
        expect(first_page.response.status).to be == 200
        expect(first_page.data["media-entries"].size).to be == 100

        next_href = first_page.json_roa_data.fetch("collection").fetch("next").fetch("href")
        query = parse_query(next_href)
        expect(query).to have_key("filter_by")
        expect(query).to have_key("page")
        expect(query["page"].first.to_i).to be == 1

        filter_by = JSON.parse(query["filter_by"].first)
        expect(filter_by).to eq(
          "meta_data" => [{ "key" => "any", "match" => match_term }],
        )

        second_page = get_by_href(next_href)
        expect(second_page.status).to be == 200
        expect(second_page.body["media-entries"]).to eq([])
      end
    end
  end
end
