require "spec_helper"

describe "filtering media entries by responsible_delegation" do
  let :media_entries_relation do
    json_roa_client.get.relation("media-entries")
  end

  def get_media_entries(filter = nil)
    media_entries_relation.get(filter).data["media-entries"]
  end

  it "returns only media entries responsible to the given delegation" do
    delegation = FactoryBot.create(:delegation)
    other_delegation = FactoryBot.create(:delegation)

    matching_entry = FactoryBot.create(
      :media_entry,
      responsible_user: nil,
      responsible_delegation: delegation,
      is_published: true,
      get_metadata_and_previews: true,
    )
    FactoryBot.create(
      :media_entry,
      responsible_user: nil,
      responsible_delegation: other_delegation,
      is_published: true,
      get_metadata_and_previews: true,
    )
    FactoryBot.create(
      :media_entry,
      responsible_user: FactoryBot.create(:user),
      is_published: true,
      get_metadata_and_previews: true,
    )

    filter = {
      permissions: [{ key: "responsible_delegation", value: delegation.id }],
    }
    fetched_media_entries = get_media_entries(
      "filter_by" => filter.deep_stringify_keys.to_json,
    )

    expect(fetched_media_entries.map { |me| me["id"] }).to eq [matching_entry.id]
  end
end
