require "spec_helper"

shared_context :bunch_of_media_entries do
  let :users_count do
    5
  end

  let :media_entries_count do
    30
  end

  let :users do
    (1..users_count).map do
      FactoryBot.create :user
    end
  end

  let :media_entries do
    (1..media_entries_count).map do
      FactoryBot.create :media_entry,
                        responsible_user: users[rand(users_count)],
                        is_published: (rand <= 0.9),
                        get_metadata_and_previews: (rand <= 0.8),
                        get_full_size: (rand <= 0.3)
    end
  end

  let :collection do
    coll = FactoryBot.create :collection
    media_entries.each do |me|
      if me.is_published && (rand <= 0.75)
        FactoryBot.create :collection_media_entry_arc,
          collection: coll, media_entry: me
      end
    end
    coll
  end
end

shared_examples "ordering by created_at" do |direction = nil|
  def media_entries_created_at(order = nil)
    # to_datetime.strftime('%Q').to_i => int with ms precision
    resource(order)
      .data["media-entries"]
      .map { |me| MediaEntry.unscoped.find(me["id"]) }
      .map { |me| me.created_at.to_datetime.strftime("%Q").to_i }
  end

  if [nil, "asc"].include?(direction)
    specify "ascending order" do
      media_entries_created_at("asc").each_cons(2) do |ca_pair|
        expect(ca_pair.first < ca_pair.second).to be true
      end
    end
  end

  if [nil, "desc"].include?(direction)
    specify "descending order" do
      media_entries_created_at("desc").each_cons(2) do |ca_pair|
        expect(ca_pair.first > ca_pair.second).to be true
      end
    end
  end
end

shared_examples "ordering by madek_core:title" do |direction = nil|
  let(:meta_key_title) do
    with_disabled_triggers do
      MetaKey.find_by(id: "madek_core:title") || FactoryBot.create(:meta_key_core_title)
    end
  end

  before do
    media_entries.map do |me|
      FactoryBot.create(:meta_datum_text,
                        media_entry: me, meta_key: meta_key_title, string: Faker::Lorem.characters(number: 16))
    end
  end

  def titles(direction = nil)
    raise ArgumentError unless [nil, "asc", "desc"].include?(direction)

    order = direction ? "title_#{direction}" : direction
    resource(order)
      .data["media-entries"]
      .map { |me| MediaEntry.unscoped.find(me["id"]) }
      .map(&:title)
  end

  if [nil, "asc"].include?(direction)
    specify "ascending order" do
      titles("asc").each_cons(2) do |pair|
        puts pair.inspect unless pair.first < pair.last
        expect(pair.first < pair.last).to be true
      end
    end
  end

  if [nil, "desc"].include?(direction)
    specify "descending order" do
      titles("desc").each_cons(2) do |pair|
        expect(pair.first > pair.last).to be true
      end
    end
  end
end

shared_examples "ordering by last_change" do
  def edit_session_updated_ats
    resource("last_change")
      .data["media-entries"]
      .map { |me| MediaEntry.unscoped.find(me["id"]) }
      .map { |me| me.edit_session_updated_at.to_datetime.strftime("%Q").to_i }
  end

  specify "ascending order" do
    edit_session_updated_ats.each_cons(2) do |pair|
      expect(pair.first < pair.last).to be true
    end
  end
end
